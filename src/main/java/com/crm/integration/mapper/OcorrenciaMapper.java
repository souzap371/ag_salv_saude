package com.crm.integration.mapper;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.model.Beneficiario;
import com.crm.integration.model.Formulario;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaStatusHistorico;
import com.crm.integration.model.Solicitante;
import com.fasterxml.jackson.databind.JsonNode;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class OcorrenciaMapper {
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private OcorrenciaMapper() {}

    public static Ocorrencia fromMosia(MosiaRequestDTO.OcorrenciaMosia dto) {
        if (dto == null) return null;

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setProtocolo(dto.getProtocolo());
        ocorrencia.setDataRegistro(converterData(dto.getDataRegistro()));
        ocorrencia.setDataAtualizacao(converterData(dto.getDataAtualizacao()));

        if (dto.getTipoOcorrencia() != null && dto.getTipoOcorrencia().getId() != null) {
            ocorrencia.setIdTipoOcorrencia(dto.getTipoOcorrencia().getId());
        }
        if (dto.getAssunto() != null) ocorrencia.setIdAssunto(dto.getAssunto().getId());
        if (dto.getSolicitante() != null) ocorrencia.setSolicitante(solicitante(dto.getSolicitante()));
        if (dto.getBeneficiario() != null) {
            ocorrencia.setBeneficiario(beneficiario(dto.getBeneficiario()));
            ocorrencia.setNumeroContrato(dto.getBeneficiario().getNumeroContrato());
        } else if (dto.getSolicitante() != null) {
            ocorrencia.setNumeroContrato(dto.getSolicitante().getNumeroContrato());
        }

        mapearFormularios(dto, ocorrencia);
        preencherBeneficiarioComDadosDosFormularios(dto, ocorrencia);
        mapearStatus(dto, ocorrencia);
        return ocorrencia;
    }

    private static void mapearFormularios(MosiaRequestDTO.OcorrenciaMosia dto, Ocorrencia ocorrencia) {
        var formularios = new ArrayList<Formulario>();
        if (dto.getSolicitacao() != null && dto.getSolicitacao().getFormularios() != null) {
            dto.getSolicitacao().getFormularios().forEach(payload -> {
                Formulario formulario = new Formulario();
                formulario.setIdForm(payload.getIdForm());
                formulario.setNome(payload.getNome());
                formulario.setFormDataJson(payload.getItens());
                formularios.add(formulario);
            });
        }
        ocorrencia.setFormularios(formularios);
    }

    private static void mapearStatus(MosiaRequestDTO.OcorrenciaMosia dto, Ocorrencia ocorrencia) {
        if (dto.getAtendimento() == null || dto.getAtendimento().getStatus() == null) return;
        var status = dto.getAtendimento().getStatus();
        ocorrencia.setStatus(status.getDescricao());
        if (status.getDescricao() != null && !status.getDescricao().isBlank()) {
            OcorrenciaStatusHistorico historico = new OcorrenciaStatusHistorico();
            historico.setStatusId(status.getId());
            historico.setDescricao(status.getDescricao());
            historico.setOrigem("MOSIA");
            ocorrencia.setHistoricoStatus(new ArrayList<>(java.util.List.of(historico)));
        }
    }

    private static Solicitante solicitante(MosiaRequestDTO.Pessoa pessoa) {
        Solicitante destino = new Solicitante();
        destino.setNome(pessoa.getNome()); destino.setChaveUnica(pessoa.getChaveUnica());
        destino.setEmail(pessoa.getEmail()); destino.setTelefone(pessoa.getTelefone());
        return destino;
    }

    private static Beneficiario beneficiario(MosiaRequestDTO.Pessoa pessoa) {
        Beneficiario destino = new Beneficiario();
        destino.setId(chaveUnicaComoId(pessoa.getChaveUnica()));
        destino.setNome(pessoa.getNome());
        destino.setEmail(pessoa.getEmail()); destino.setTelefone(pessoa.getTelefone());
        return destino;
    }

    private static void preencherBeneficiarioComDadosDosFormularios(
            MosiaRequestDTO.OcorrenciaMosia dto,
            Ocorrencia ocorrencia) {
        Beneficiario beneficiario = ocorrencia.getBeneficiario();
        if (beneficiario == null
                || dto.getSolicitacao() == null
                || dto.getSolicitacao().getFormularios() == null) {
            return;
        }

        for (MosiaRequestDTO.FormularioPayload formulario
                : dto.getSolicitacao().getFormularios()) {
            JsonNode itens = formulario.getItens();
            if (itens == null || itens.isNull()) continue;

            if (estaVazio(beneficiario.getNome())) {
                beneficiario.setNome(buscarValor(itens, Set.of("nome", "nomebeneficiario")));
            }
            if (estaVazio(beneficiario.getTelefone())) {
                beneficiario.setTelefone(buscarValor(
                        itens,
                        Set.of("telefonecelular", "telefone", "celular", "fone")));
            }
            if (estaVazio(beneficiario.getEmail())) {
                beneficiario.setEmail(buscarValor(itens, Set.of("email", "emailbeneficiario")));
            }

            if (!estaVazio(beneficiario.getNome())
                    && !estaVazio(beneficiario.getTelefone())
                    && !estaVazio(beneficiario.getEmail())) {
                return;
            }
        }
    }

    private static String buscarValor(JsonNode node, Set<String> nomesAceitos) {
        if (node == null || node.isNull()) return null;

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> campos = node.fields();
            while (campos.hasNext()) {
                Map.Entry<String, JsonNode> campo = campos.next();
                JsonNode valor = campo.getValue();
                if (nomesAceitos.contains(normalizarNomeCampo(campo.getKey()))
                        && valor != null
                        && valor.isValueNode()
                        && !valor.asText().isBlank()) {
                    return valor.asText().trim();
                }
            }
            campos = node.fields();
            while (campos.hasNext()) {
                String encontrado = buscarValor(campos.next().getValue(), nomesAceitos);
                if (!estaVazio(encontrado)) return encontrado;
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                String encontrado = buscarValor(item, nomesAceitos);
                if (!estaVazio(encontrado)) return encontrado;
            }
        }
        return null;
    }

    private static String normalizarNomeCampo(String nome) {
        if (nome == null) return "";
        return Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean estaVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String chaveUnicaComoId(String chaveUnica) {
        if (chaveUnica == null || chaveUnica.isBlank()) {
            throw new IllegalArgumentException(
                    "A chaveUnica do beneficiário é obrigatória para identificar o beneficiário.");
        }
        return chaveUnica.trim();
    }

    private static java.time.LocalDateTime converterData(String valor) {
        if (valor == null || valor.isBlank()) return java.time.LocalDateTime.now(ZONE_ID);
        try { return OffsetDateTime.parse(valor).atZoneSameInstant(ZONE_ID).toLocalDateTime(); }
        catch (Exception ignored) { return java.time.LocalDateTime.now(ZONE_ID); }
    }
}
