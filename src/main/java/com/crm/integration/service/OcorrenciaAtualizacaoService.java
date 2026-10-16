package com.crm.integration.service;

import com.crm.integration.model.MensagemRequest;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaStatusHistorico;
import com.crm.integration.repository.AgMensAppRepository;
import com.crm.integration.repository.OcorrenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;

@Service
public class OcorrenciaAtualizacaoService {
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final int TAMANHO_MAXIMO_MENSAGEM = 255;
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> CAMPOS_NUMERO_GUIA = Set.of(
            "nnumeguia", "numeroguia", "numerodaguia");

    private final OcorrenciaRepository ocorrenciaRepository;
    private final AgMensAppRepository mensagemRepository;
    private final MensageriaService mensageriaService;
    private final MosiaIntegrationService mosiaIntegrationService;
    private final MosiaStatusConfiguracaoService statusConfiguracaoService;

    public OcorrenciaAtualizacaoService(
            OcorrenciaRepository ocorrenciaRepository,
            AgMensAppRepository mensagemRepository,
            MensageriaService mensageriaService,
            MosiaIntegrationService mosiaIntegrationService,
            MosiaStatusConfiguracaoService statusConfiguracaoService) {
        this.ocorrenciaRepository = ocorrenciaRepository;
        this.mensagemRepository = mensagemRepository;
        this.mensageriaService = mensageriaService;
        this.mosiaIntegrationService = mosiaIntegrationService;
        this.statusConfiguracaoService = statusConfiguracaoService;
    }

    @Transactional
    public Resultado atualizarStatus(
            String protocolo,
            Integer statusId,
            String descricaoInformada,
            String chaveInformada,
            Long numeroGuiaInformado) {
        Ocorrencia ocorrencia = ocorrenciaRepository
                .findTopByProtocoloOrderByDataAtualizacaoDescIdDesc(protocolo)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(protocolo));

        String descricao = normalizar(descricaoInformada);
        if (descricao == null) {
            descricao = statusConfiguracaoService.buscarDescricao(ocorrencia.getIdTipoOcorrencia(), statusId);
        }
        if (descricao == null) descricao = String.valueOf(statusId);

        String chaveUnica = obterChaveUnica(ocorrencia, chaveInformada);
        if (chaveUnica == null) {
            throw new IllegalArgumentException("A ocorrência não possui chave única de beneficiário ou solicitante.");
        }

        boolean statusAlterado = !descricao.equalsIgnoreCase(normalizar(ocorrencia.getStatus()));
        boolean sincronizado = mosiaIntegrationService.atualizarStatusNoMosia(
                protocolo, statusId, descricao, chaveUnica);

        ocorrencia.setStatus(descricao);
        ocorrencia.setDataAtualizacao(LocalDateTime.now(ZONE_ID));
        if (statusAlterado) adicionarHistorico(ocorrencia, statusId, descricao, "SOLUS");
        ocorrenciaRepository.save(ocorrencia);

        Long numeroGuia = numeroGuiaInformado != null ? numeroGuiaInformado : buscarNumeroGuia(ocorrencia);
        boolean mensagemCriada = statusAlterado
                && enfileirarMensagem(ocorrencia, statusId, descricao, chaveUnica, numeroGuia);
        return new Resultado(ocorrencia, sincronizado, mensagemCriada);
    }

    public Resultado atualizarStatus(
            String protocolo, Integer statusId, String descricaoInformada, String chaveInformada) {
        return atualizarStatus(protocolo, statusId, descricaoInformada, chaveInformada, null);
    }

    @Transactional
    public Ocorrencia registrarStatusRecebidoDoMosia(String protocolo, String statusId, String descricao) {
        Ocorrencia ocorrencia = ocorrenciaRepository
                .findTopByProtocoloOrderByDataAtualizacaoDescIdDesc(protocolo)
                .orElseThrow(() -> new OcorrenciaNaoEncontradaException(protocolo));
        String descricaoNormalizada = normalizar(descricao);
        if (descricaoNormalizada == null) return ocorrencia;
        if (!descricaoNormalizada.equalsIgnoreCase(normalizar(ocorrencia.getStatus()))) {
            ocorrencia.setStatus(descricaoNormalizada);
            ocorrencia.setDataAtualizacao(LocalDateTime.now(ZONE_ID));
            adicionarHistorico(ocorrencia, converterInteiro(statusId), descricaoNormalizada, "MOSIA");
            ocorrenciaRepository.save(ocorrencia);
        }
        return ocorrencia;
    }

    private boolean enfileirarMensagem(
            Ocorrencia ocorrencia,
            Integer statusId,
            String descricao,
            String chaveUnica,
            Long numeroGuia) {
        String identificador = limitar(ocorrencia.getProtocolo() + ":" + statusId + ":" + normalizarChave(descricao), 255);
        if (mensagemRepository.existsByIdRegistro(identificador)) return false;

        MensagemRequest request = new MensagemRequest();
        request.setChaveUnica(chaveUnica);
        request.setMsg(limitar(criarMensagem(ocorrencia, descricao), TAMANHO_MAXIMO_MENSAGEM));
        mensageriaService.enfileirarMensagem(
                request, ocorrencia.getIdFuncionalidade(), identificador, numeroGuia);
        return true;
    }

    private Long buscarNumeroGuia(Ocorrencia ocorrencia) {
        if (ocorrencia.getFormularios() == null) return null;
        for (var formulario : ocorrencia.getFormularios()) {
            if (formulario.getDadosJson() == null || formulario.getDadosJson().isBlank()) continue;
            try {
                Long numero = buscarNumeroGuia(JSON.readTree(formulario.getDadosJson()));
                if (numero != null) return numero;
            } catch (Exception ignored) {
                // JSON inválido não deve impedir a atualização do status.
            }
        }
        return null;
    }

    private Long buscarNumeroGuia(JsonNode node) {
        if (node == null || node.isNull()) return null;
        if (node.isObject()) {
            var campos = node.fields();
            while (campos.hasNext()) {
                var campo = campos.next();
                if (CAMPOS_NUMERO_GUIA.contains(normalizarNomeCampo(campo.getKey()))) {
                    Long numero = converterLong(campo.getValue());
                    if (numero != null) return numero;
                }
                Long numero = buscarNumeroGuia(campo.getValue());
                if (numero != null) return numero;
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                Long numero = buscarNumeroGuia(item);
                if (numero != null) return numero;
            }
        }
        return null;
    }

    private Long converterLong(JsonNode node) {
        if (node == null || node.isNull()) return null;
        String valor = node.asText("").replaceAll("\\D", "");
        try { return valor.isBlank() ? null : Long.valueOf(valor); }
        catch (NumberFormatException ignored) { return null; }
    }

    private String normalizarNomeCampo(String valor) {
        return java.text.Normalizer.normalize(valor == null ? "" : valor, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(java.util.Locale.ROOT);
    }

    private String criarMensagem(Ocorrencia ocorrencia, String status) {
        String tipo = normalizar(ocorrencia.getTipoNegocio());
        String protocolo = ocorrencia.getProtocolo();
        if ("SOLICITACAO_AUTORIZACAO".equalsIgnoreCase(tipo)) {
            return "Sua solicitação de autorização, protocolo " + protocolo + ", foi atualizada para: " + status + ".";
        }
        if ("ATUALIZACAO_CADASTRAL".equalsIgnoreCase(tipo)) {
            return "Sua atualização cadastral, protocolo " + protocolo + ", foi atualizada para: " + status + ".";
        }
        if ("REEMBOLSO".equalsIgnoreCase(tipo)) {
            return "Sua solicitação de reembolso, protocolo " + protocolo + ", foi atualizada para: " + status + ".";
        }
        return "A ocorrência " + (tipo == null ? "" : "\"" + tipo + "\" ")
                + "do protocolo " + protocolo + " foi atualizada para: " + status + ".";
    }

    private void adicionarHistorico(Ocorrencia ocorrencia, Integer statusId, String descricao, String origem) {
        OcorrenciaStatusHistorico historico = new OcorrenciaStatusHistorico();
        historico.setStatusId(statusId == null ? null : String.valueOf(statusId));
        historico.setDescricao(descricao);
        historico.setOrigem(origem);
        historico.setOcorrencia(ocorrencia);
        ocorrencia.getHistoricoStatus().add(historico);
    }

    private String obterChaveUnica(Ocorrencia ocorrencia, String informada) {
        if (ocorrencia.getBeneficiario() != null && normalizar(ocorrencia.getBeneficiario().getId()) != null)
            return ocorrencia.getBeneficiario().getId().trim();
        if (ocorrencia.getSolicitante() != null && normalizar(ocorrencia.getSolicitante().getChaveUnica()) != null)
            return ocorrencia.getSolicitante().getChaveUnica().trim();
        return normalizar(informada);
    }

    private String normalizar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private String normalizarChave(String valor) {
        return valor == null ? "status" : valor.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
    private String limitar(String valor, int tamanho) {
        return valor.length() <= tamanho ? valor : valor.substring(0, tamanho);
    }
    private Integer converterInteiro(String valor) {
        try { return valor == null ? null : Integer.valueOf(valor); }
        catch (NumberFormatException ignored) { return null; }
    }

    public record Resultado(Ocorrencia ocorrencia, boolean sincronizadoMosia, boolean mensagemCriada) {}

    public static class OcorrenciaNaoEncontradaException extends RuntimeException {
        public OcorrenciaNaoEncontradaException(String protocolo) {
            super("Ocorrência não encontrada para o protocolo " + protocolo + ".");
        }
    }
}
