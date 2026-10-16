package com.crm.integration.service;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.enums.TipoNegocioOcorrencia;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaTipoConfiguracao;
import com.crm.integration.repository.OcorrenciaTipoConfiguracaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class OcorrenciaClassificacaoService {
    private static final Logger log = LoggerFactory.getLogger(OcorrenciaClassificacaoService.class);

    private final OcorrenciaTipoConfiguracaoRepository repository;

    public OcorrenciaClassificacaoService(OcorrenciaTipoConfiguracaoRepository repository) {
        this.repository = repository;
    }

    public String classificar(Ocorrencia ocorrencia) {
        String tipo = encontrarConfiguracao(ocorrencia)
                .map(OcorrenciaTipoConfiguracao::getTipoNegocio)
                .orElse(TipoNegocioOcorrencia.OUTRA.name());
        ocorrencia.setTipoNegocio(tipo);
        return tipo;
    }

    /**
     * Usa primeiro as associações já configuradas. Quando ainda não existe uma
     * associação, tenta descobrir a finalidade pelos textos enviados pelo Mosia
     * e registra o identificador mais estável para os próximos callbacks.
     */
    public String classificarERegistrar(
            Ocorrencia ocorrencia,
            MosiaRequestDTO payload) {
        Optional<OcorrenciaTipoConfiguracao> configuracao = encontrarConfiguracao(ocorrencia);
        if (configuracao.isPresent()) {
            OcorrenciaTipoConfiguracao configuracaoEncontrada = configuracao.get();
            enriquecerConfiguracaoPorCallback(configuracaoEncontrada, ocorrencia);
            String tipo = configuracaoEncontrada.getTipoNegocio();
            ocorrencia.setTipoNegocio(tipo);
            return tipo;
        }

        String tipoInferido = inferirTipo(payload);
        ocorrencia.setTipoNegocio(tipoInferido);

        if (!TipoNegocioOcorrencia.OUTRA.name().equals(tipoInferido)) {
            registrarAssociacaoDescoberta(ocorrencia, tipoInferido);
        }
        return tipoInferido;
    }

    private Optional<OcorrenciaTipoConfiguracao> encontrarConfiguracao(Ocorrencia ocorrencia) {
        return repository.findByAtivoTrue().stream()
                .filter(config -> corresponde(config, ocorrencia))
                .max(Comparator.comparingInt(this::especificidade));
    }

    private String inferirTipo(MosiaRequestDTO payload) {
        String texto = textoClassificacao(payload);
        if (texto.contains("reembolso")) {
            return TipoNegocioOcorrencia.REEMBOLSO.name();
        }
        if (texto.contains("atualizacao cadastral")
                || texto.contains("atualizacao de cadastro")
                || texto.contains("alteracao cadastral")
                || texto.contains("alteracao de cadastro")
                || texto.contains("atualizar cadastro")) {
            return TipoNegocioOcorrencia.ATUALIZACAO_CADASTRAL.name();
        }
        if (texto.contains("autorizacao")) {
            return TipoNegocioOcorrencia.SOLICITACAO_AUTORIZACAO.name();
        }
        String nomenclaturaMosia = nomenclaturaMosia(payload);
        return nomenclaturaMosia == null ? TipoNegocioOcorrencia.OUTRA.name() : nomenclaturaMosia;
    }

    private String nomenclaturaMosia(MosiaRequestDTO payload) {
        if (payload == null) return null;
        if (payload.getOcorrencia() != null) {
            MosiaRequestDTO.OcorrenciaMosia mosia = payload.getOcorrencia();
            String valor = textoOriginal(mosia.getTipoOcorrencia() == null
                    ? null : mosia.getTipoOcorrencia().getDescricao());
            if (valor != null) return valor;
        }
        String valor = textoOriginal(payload.getFuncionalidade() == null
                ? null : payload.getFuncionalidade().getDescricao());
        if (valor != null) return valor;
        if (payload.getOcorrencia() == null) return null;

        MosiaRequestDTO.OcorrenciaMosia mosia = payload.getOcorrencia();
        if (mosia.getSolicitacao() != null && mosia.getSolicitacao().getFormularios() != null) {
            valor = mosia.getSolicitacao().getFormularios().stream()
                    .map(MosiaRequestDTO.FormularioPayload::getNome)
                    .map(this::textoOriginal)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            if (valor != null) return valor;
        }
        valor = textoOriginal(mosia.getAssunto() == null ? null : mosia.getAssunto().getDescricao());
        if (valor != null) return valor;
        return textoOriginal(mosia.getAtendimento() == null || mosia.getAtendimento().getStatus() == null
                ? null : mosia.getAtendimento().getStatus().getDescricao());
    }

    private String textoClassificacao(MosiaRequestDTO payload) {
        StringBuilder texto = new StringBuilder();
        if (payload == null) return "";

        if (payload.getFuncionalidade() != null) {
            adicionar(texto, payload.getFuncionalidade().getDescricao());
        }
        if (payload.getOcorrencia() == null) return normalizar(texto.toString());

        MosiaRequestDTO.OcorrenciaMosia mosia = payload.getOcorrencia();
        if (mosia.getTipoOcorrencia() != null) adicionar(texto, mosia.getTipoOcorrencia().getDescricao());
        if (mosia.getAssunto() != null) adicionar(texto, mosia.getAssunto().getDescricao());
        if (mosia.getAtendimento() != null && mosia.getAtendimento().getStatus() != null) {
            adicionar(texto, mosia.getAtendimento().getStatus().getDescricao());
        }
        if (mosia.getSolicitacao() != null && mosia.getSolicitacao().getFormularios() != null) {
            mosia.getSolicitacao().getFormularios().stream()
                    .map(MosiaRequestDTO.FormularioPayload::getNome)
                    .forEach(nome -> adicionar(texto, nome));
        }
        return normalizar(texto.toString());
    }

    private void registrarAssociacaoDescoberta(
            Ocorrencia ocorrencia,
            String tipoInferido) {
        List<OcorrenciaTipoConfiguracao> configuracoes = repository.findByAtivoTrue();
        String funcionalidade = textoOuNull(ocorrencia.getIdFuncionalidade());

        if (funcionalidade != null) {
            Optional<OcorrenciaTipoConfiguracao> existente = configuracoes.stream()
                    .filter(config -> funcionalidade.equals(config.getIdFuncionalidade()))
                    .findFirst();
            if (existente.isPresent()) {
                if (!existente.get().getTipoNegocio().equals(tipoInferido)) {
                    log.warn("Associação automática ignorada: funcionalidade={} já pertence a {} e o callback sugeriu {}",
                            funcionalidade, existente.get().getTipoNegocio(), tipoInferido);
                }
                return;
            }
            salvarConfiguracao(
                    tipoInferido,
                    funcionalidade,
                    positivoOuNull(ocorrencia.getIdTipoOcorrencia()),
                    positivoOuNull(ocorrencia.getIdAssunto()));
            return;
        }

        Integer idTipoOcorrencia = positivoOuNull(ocorrencia.getIdTipoOcorrencia());
        if (idTipoOcorrencia != null) {
            boolean existente = configuracoes.stream()
                    .anyMatch(config -> idTipoOcorrencia.equals(config.getIdTipoOcorrencia()));
            if (!existente) salvarConfiguracao(tipoInferido, null, idTipoOcorrencia, null);
            return;
        }

        Integer idAssunto = positivoOuNull(ocorrencia.getIdAssunto());
        if (idAssunto != null) {
            boolean existente = configuracoes.stream()
                    .anyMatch(config -> idAssunto.equals(config.getIdAssunto()));
            if (!existente) salvarConfiguracao(tipoInferido, null, null, idAssunto);
        } else {
            log.warn("Tipo {} reconhecido, mas o callback não trouxe um identificador para registrar a associação",
                    tipoInferido);
        }
    }

    private void enriquecerConfiguracaoPorCallback(
            OcorrenciaTipoConfiguracao configuracao,
            Ocorrencia ocorrencia) {
        // Uma associação baseada em funcionalidade foi descoberta automaticamente.
        // Completa os IDs que o Mosia passou a fornecer nos callbacks seguintes.
        if (configuracao.getIdFuncionalidade() == null) return;

        boolean alterada = false;
        Integer idTipoOcorrencia = positivoOuNull(ocorrencia.getIdTipoOcorrencia());
        Integer idAssunto = positivoOuNull(ocorrencia.getIdAssunto());
        if (configuracao.getIdTipoOcorrencia() == null && idTipoOcorrencia != null) {
            configuracao.setIdTipoOcorrencia(idTipoOcorrencia);
            alterada = true;
        }
        if (configuracao.getIdAssunto() == null && idAssunto != null) {
            configuracao.setIdAssunto(idAssunto);
            alterada = true;
        }
        if (alterada) {
            repository.save(configuracao);
            log.info("Associação de ocorrência complementada pelo callback: tipo={} funcionalidade={} tipoMosia={} assunto={}",
                    configuracao.getTipoNegocio(), configuracao.getIdFuncionalidade(),
                    configuracao.getIdTipoOcorrencia(), configuracao.getIdAssunto());
        }
    }

    private void salvarConfiguracao(
            String tipo,
            String funcionalidade,
            Integer idTipoOcorrencia,
            Integer idAssunto) {
        OcorrenciaTipoConfiguracao configuracao = new OcorrenciaTipoConfiguracao();
        configuracao.setTipoNegocio(tipo);
        configuracao.setIdFuncionalidade(funcionalidade);
        configuracao.setIdTipoOcorrencia(idTipoOcorrencia);
        configuracao.setIdAssunto(idAssunto);
        configuracao.setAtivo(true);
        repository.save(configuracao);
        log.info("Associação de ocorrência descoberta automaticamente: tipo={} funcionalidade={} tipoMosia={} assunto={}",
                tipo, funcionalidade, idTipoOcorrencia, idAssunto);
    }

    private void adicionar(StringBuilder destino, String valor) {
        if (valor != null && !valor.isBlank()) destino.append(' ').append(valor);
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor == null ? "" : valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
    }

    private String textoOuNull(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    private String textoOriginal(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    private Integer positivoOuNull(Integer valor) {
        return valor != null && valor > 0 ? valor : null;
    }

    private boolean corresponde(OcorrenciaTipoConfiguracao config, Ocorrencia ocorrencia) {
        return (config.getIdFuncionalidade() == null || config.getIdFuncionalidade().equals(ocorrencia.getIdFuncionalidade()))
                && (config.getIdTipoOcorrencia() == null || config.getIdTipoOcorrencia().equals(ocorrencia.getIdTipoOcorrencia()))
                && (config.getIdAssunto() == null || config.getIdAssunto().equals(ocorrencia.getIdAssunto()));
    }

    private int especificidade(OcorrenciaTipoConfiguracao config) {
        int pontos = 0;
        if (config.getIdFuncionalidade() != null) pontos++;
        if (config.getIdTipoOcorrencia() != null) pontos++;
        if (config.getIdAssunto() != null) pontos++;
        return pontos;
    }
}
