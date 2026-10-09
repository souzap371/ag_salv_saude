package com.crm.integration.service;

import com.crm.integration.model.AutorizacaoArquivo;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.repository.AutorizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AutorizacaoService {

    private static final Logger log = LoggerFactory.getLogger(AutorizacaoService.class);

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final AutorizacaoRepository autorizacaoRepo;
    private final StorageService storageService;
    private final MosiaIntegrationService mosiaIntegrationService;

    public AutorizacaoService(
            AutorizacaoRepository autorizacaoRepo,
            StorageService storageService,
            MosiaIntegrationService mosiaIntegrationService) {

        this.autorizacaoRepo = autorizacaoRepo;
        this.storageService = storageService;
        this.mosiaIntegrationService = mosiaIntegrationService;
    }

    /**
     * Salva uma autorização recebida pelo callback da Mosia.
     *
     * Como a informação veio da própria Mosia, ela já deve ser
     * considerada sincronizada e não pode entrar na fila do scheduler.
     */
    @Transactional
    public void saveAutorizacao(
            AutorizacaoSolicitada entity) {

        if (entity == null) {
            throw new IllegalArgumentException(
                    "A autorização não pode ser nula.");
        }

        String protocolo = normalizar(entity.getProtocolo());

        if (protocolo == null) {
            throw new IllegalArgumentException(
                    "O protocolo da autorização é obrigatório.");
        }

        entity.setProtocolo(protocolo);

        baixarArquivos(entity);
        preencherStatusIdQuandoNecessario(entity);

        OffsetDateTime agora = OffsetDateTime.now(ZONE_ID);

        /*
         * Esse método é utilizado pelo callback da Mosia.
         * Portanto, o registro já está sincronizado.
         */
        entity.setSincronizadoMosia("S");
        entity.setUltimaSincronizacao(agora);

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(agora);
        }

        entity.setUpdatedAt(agora);

        autorizacaoRepo.saveAndFlush(entity);

        /*
         * Encerra também outras versões pendentes do mesmo protocolo.
         */
        int registrosAtualizados = autorizacaoRepo
                .marcarProtocoloComoSincronizado(
                        protocolo,
                        agora);

        log.info(
                "[SAVE_AUTORIZACAO] Callback salvo. "
                        + "protocolo={} status='{}' statusId={} "
                        + "registrosSincronizados={}",
                protocolo,
                entity.getStatus(),
                entity.getStatusId(),
                registrosAtualizados);
    }

    /**
     * Retorna todo o histórico de um protocolo.
     */
    @Transactional(readOnly = true)
    public List<AutorizacaoSolicitada> findAllByProtocolo(String protocolo) {

        return autorizacaoRepo
                .findAllByProtocoloOrderByCreatedAtAsc(
                        protocolo);
    }

    /**
     * Retorna o registro mais recente de um protocolo.
     */
    @Transactional(readOnly = true)
    public Optional<AutorizacaoSolicitada> findUltimaPorProtocolo(String protocolo) {

        return autorizacaoRepo
                .findTopByProtocoloOrderByCreatedAtDesc(
                        protocolo);
    }

    /**
     * Registra uma alteração originada pelo sistema local.
     *
     * Esse registro é salvo com SINCRONIZADO_MOSIA = N.
     * O scheduler será o único responsável pelo envio à Mosia.
     */
    @Transactional
    public AutorizacaoSolicitada registrarNovoStatus(
            String protocolo,
            String novoStatus) {

        String protocoloNormalizado = normalizar(protocolo);

        String statusNormalizado = normalizar(novoStatus);

        if (protocoloNormalizado == null) {
            throw new IllegalArgumentException(
                    "O protocolo é obrigatório.");
        }

        if (statusNormalizado == null) {
            throw new IllegalArgumentException(
                    "O status é obrigatório.");
        }

        Optional<AutorizacaoSolicitada> ultimaOpt = autorizacaoRepo
                .findTopByProtocoloOrderByCreatedAtDesc(
                        protocoloNormalizado);

        AutorizacaoSolicitada novo = new AutorizacaoSolicitada();

        if (ultimaOpt.isPresent()) {
            copiarDadosDaUltimaVersao(
                    ultimaOpt.get(),
                    novo);
        }

        Integer statusId = converterOuMapearStatusId(
                statusNormalizado);

        OffsetDateTime agora = OffsetDateTime.now(ZONE_ID);

        novo.setProtocolo(protocoloNormalizado);
        novo.setStatus(statusNormalizado);

        novo.setStatusId(
                statusId != null
                        ? String.valueOf(statusId)
                        : null);

        /*
         * A alteração nasceu no sistema local.
         * Deve entrar na fila do scheduler.
         */
        novo.setSincronizadoMosia("N");
        novo.setUltimaSincronizacao(null);
        novo.setCreatedAt(agora);
        novo.setUpdatedAt(agora);

        AutorizacaoSolicitada salvo = autorizacaoRepo.saveAndFlush(novo);

        log.info(
                "[REGISTRAR_NOVO_STATUS] Pendência criada. "
                        + "id={} protocolo={} status='{}' statusId={}",
                salvo.getId(),
                salvo.getProtocolo(),
                salvo.getStatus(),
                salvo.getStatusId());

        return salvo;
    }

    /**
     * Mantém compatibilidade com chamadas antigas.
     */
    @Transactional
    public void updateStatusLocal(
            String protocolo,
            String novoStatus) {

        registrarNovoStatus(
                protocolo,
                novoStatus);
    }

    private void baixarArquivos(
            AutorizacaoSolicitada entity) {

        if (entity.getArquivos() == null) {
            return;
        }

        for (AutorizacaoArquivo arquivo : entity.getArquivos()) {

            if (arquivo == null) {
                continue;
            }

            if (arquivo.getUrlOriginal() == null
                    || arquivo.getUrlOriginal().isBlank()) {
                continue;
            }

            if (arquivo.getCimaImagem() != null
                    && arquivo.getCimaImagem().length > 0) {
                arquivo.setAutorizacao(entity);
                continue;
            }

            try {
                StorageService.ArquivoBaixado arquivoBaixado = storageService.download(
                        arquivo.getUrlOriginal());

                arquivo.setCimaImagem(arquivoBaixado.conteudo());
                arquivo.setCaminhoLocal(null);
                if (arquivo.getNomeOriginal() == null || arquivo.getNomeOriginal().isBlank()) {
                    arquivo.setNomeOriginal(arquivoBaixado.nome());
                }
                arquivo.setBaixadoEm(
                        OffsetDateTime.now(ZONE_ID));

                arquivo.setAutorizacao(entity);

            } catch (Exception e) {
                log.error(
                        "Falha ao baixar anexo para o banco. "
                                + "protocolo={} erro={}",
                        entity.getProtocolo(),
                        e.getMessage(),
                        e);
            }
        }
    }

    private void preencherStatusIdQuandoNecessario(
            AutorizacaoSolicitada entity) {

        if (entity.getStatusId() != null
                && !entity.getStatusId().isBlank()) {
            return;
        }

        String status = normalizar(entity.getStatus());

        if (status == null) {
            return;
        }

        Integer statusId = converterOuMapearStatusId(status);

        if (statusId != null) {
            entity.setStatusId(
                    String.valueOf(statusId));
        }
    }

    private Integer converterOuMapearStatusId(
            String status) {

        if (status == null || status.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(status.trim());

        } catch (NumberFormatException ignored) {
            return mosiaIntegrationService
                    .mapearCodigoPorNomePublic(status);
        }
    }

    private void copiarDadosDaUltimaVersao(
            AutorizacaoSolicitada origem,
            AutorizacaoSolicitada destino) {

        destino.setChaveUnica(
                origem.getChaveUnica());

        destino.setNumeroContrato(
                origem.getNumeroContrato());

        destino.setNome(
                origem.getNome());

        destino.setTipoGuia(
                origem.getTipoGuia());

        destino.setIdTipoGuia(
                origem.getIdTipoGuia());

        destino.setLabel(
                origem.getLabel());

        destino.setTelefoneContato(
                origem.getTelefoneContato());

        destino.setEmailContato(
                origem.getEmailContato());

        destino.setObservacao(
                origem.getObservacao());

        destino.setRawCallbackJson(
                origem.getRawCallbackJson());

        List<AutorizacaoArquivo> arquivosClonados = new ArrayList<>();

        if (origem.getArquivos() != null) {
            for (AutorizacaoArquivo arquivo : origem.getArquivos()) {

                if (arquivo == null) {
                    continue;
                }

                AutorizacaoArquivo clone = new AutorizacaoArquivo();

                clone.setNomeOriginal(
                        arquivo.getNomeOriginal());

                clone.setUrlOriginal(
                        arquivo.getUrlOriginal());

                clone.setCaminhoLocal(
                        arquivo.getCaminhoLocal());

                clone.setBaixadoEm(
                        arquivo.getBaixadoEm());

                clone.setCimaImagem(
                        arquivo.getCimaImagem());

                clone.setAutorizacao(destino);

                arquivosClonados.add(clone);
            }
        }

        destino.setArquivos(
                arquivosClonados);
    }

    private String normalizar(String valor) {

        if (valor == null) {
            return null;
        }

        String normalizado = valor.trim();

        return normalizado.isEmpty()
                ? null
                : normalizado;
    }
}
