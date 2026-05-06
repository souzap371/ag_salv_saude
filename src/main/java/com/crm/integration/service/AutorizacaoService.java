package com.crm.integration.service;

import com.crm.integration.model.AutorizacaoArquivo;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.repository.AutorizacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AutorizacaoService {

    private static final Logger log = LoggerFactory.getLogger(AutorizacaoService.class);

    private final AutorizacaoRepository autorizacaoRepo;
    private final StorageService storageService;
    private final AutorizacaoNotifierService notifierService;
    private final MosiaIntegrationService mosiaIntegrationService;

    public AutorizacaoService(AutorizacaoRepository autorizacaoRepo,
            StorageService storageService,
            AutorizacaoNotifierService notifierService,
            MosiaIntegrationService mosiaIntegrationService) {
        this.autorizacaoRepo = autorizacaoRepo;
        this.storageService = storageService;
        this.notifierService = notifierService;
        this.mosiaIntegrationService = mosiaIntegrationService;
    }

    /**
     * Salva uma nova autorização recebida via callback.
     * Baixa arquivos caso ainda não tenham sido baixados.
     * Se statusId estiver ausente mas houver status textual, tenta mapear para id.
     */
    @Transactional
    public void saveAutorizacao(AutorizacaoSolicitada entity) {
        // baixa arquivos se necessário
        if (entity.getArquivos() != null) {
            for (AutorizacaoArquivo ar : entity.getArquivos()) {
                if (ar.getUrlOriginal() != null && ar.getCaminhoLocal() == null) {
                    try {
                        String folder = "/uploads/autorizacoes";
                        String localPath = storageService.downloadAndSave(ar.getUrlOriginal());
                        ar.setCaminhoLocal(localPath);
                        ar.setBaixadoEm(OffsetDateTime.now());
                        ar.setAutorizacao(entity);
                    } catch (Exception e) {
                        log.error("Falha ao baixar arquivo {} : {}", ar.getUrlOriginal(), e.getMessage(), e);
                        
                    }
                }
            }
        }

        // Se tiver status textual mas não statusId, tenta mapear via
        // MosiaIntegrationService
        if ((entity.getStatusId() == null || entity.getStatusId().isBlank()) && entity.getStatus() != null) {
            Integer mapped = mosiaIntegrationService.mapearCodigoPorNomePublic(entity.getStatus());
            if (mapped != null) {
                entity.setStatusId(String.valueOf(mapped));
                log.debug("Preenchido statusId via mapeamento: {} -> {}", entity.getStatus(), entity.getStatusId());
            } else {
                log.debug("Não foi possível mapear status '{}' para id", entity.getStatus());
            }
        }

        // Logging antes do save para inspeção
        log.info("[SAVE_AUTORIZACAO] protocolo={} status='{}' status_id={} arquivos={}",
                entity.getProtocolo(),
                entity.getStatus(),
                entity.getStatusId(),
                entity.getArquivos() != null ? entity.getArquivos().size() : 0);

        // marca sincronizado_mosia default se vier nulo (ex: callbacks do MOSIA já
        // podem estar sincronizados)
        if (entity.getSincronizadoMosia() == null) {
            entity.setSincronizadoMosia("S");
        }

        autorizacaoRepo.saveAndFlush(entity);
    }

    public List<AutorizacaoSolicitada> findAllByProtocolo(String protocolo) {
        return autorizacaoRepo.findAllByProtocoloOrderByCreatedAtAsc(protocolo);
    }

    public Optional<AutorizacaoSolicitada> findUltimaPorProtocolo(String protocolo) {
        return autorizacaoRepo.findTopByProtocoloOrderByCreatedAtDesc(protocolo);
    }

    /**
     * Cria um novo registro para um protocolo com novo status, mantendo histórico.
     * Clona arquivos e campos relevantes da última versão.
     */
    @Transactional
    public AutorizacaoSolicitada registrarNovoStatus(String protocolo, String novoStatus) {
        Optional<AutorizacaoSolicitada> ultimaOpt = autorizacaoRepo.findTopByProtocoloOrderByCreatedAtDesc(protocolo);
        AutorizacaoSolicitada novo = new AutorizacaoSolicitada();

        if (ultimaOpt.isPresent()) {
            AutorizacaoSolicitada ultima = ultimaOpt.get();

            // Copia campos relevantes
            novo.setChaveUnica(ultima.getChaveUnica());
            novo.setNumeroContrato(ultima.getNumeroContrato());
            novo.setNome(ultima.getNome());
            novo.setTipoGuia(ultima.getTipoGuia());
            novo.setIdTipoGuia(ultima.getIdTipoGuia());
            novo.setLabel(ultima.getLabel());
            novo.setTelefoneContato(ultima.getTelefoneContato());
            novo.setEmailContato(ultima.getEmailContato());
            novo.setObservacao(ultima.getObservacao());
            novo.setRawCallbackJson(ultima.getRawCallbackJson());

            // Clona arquivos para evitar shared references
            List<AutorizacaoArquivo> arquivosClonados = new ArrayList<>();
            if (ultima.getArquivos() != null) {
                for (AutorizacaoArquivo ar : ultima.getArquivos()) {
                    AutorizacaoArquivo clone = new AutorizacaoArquivo();
                    clone.setNomeOriginal(ar.getNomeOriginal());
                    clone.setUrlOriginal(ar.getUrlOriginal());
                    clone.setCaminhoLocal(ar.getCaminhoLocal());
                    clone.setBaixadoEm(ar.getBaixadoEm());
                    clone.setCimaImagem(ar.getCimaImagem());
                    clone.setAutorizacao(novo); // vincula ao novo registro
                    arquivosClonados.add(clone);
                }
            }
            novo.setArquivos(arquivosClonados);
        }

        // Preenche protocolo e status textual
        novo.setProtocolo(protocolo);
        novo.setStatus(novoStatus);

        // tenta identificar statusId a partir de novoStatus (pode ser id em string ou
        // nome)
        String statusId = null;
        if (novoStatus != null) {
            // se já vier numérico
            try {
                Integer.parseInt(novoStatus);
                statusId = novoStatus;
            } catch (NumberFormatException e) {
                Integer id = mosiaIntegrationService.mapearCodigoPorNomePublic(novoStatus);
                statusId = id != null ? String.valueOf(id) : null;
            }
        }
        novo.setStatusId(statusId);

        // marca como não sincronizado por padrão (sera true se notifierService enviar
        // com sucesso)
        novo.setSincronizadoMosia("N");

        // Persistir no banco (gera novo histórico)
        autorizacaoRepo.saveAndFlush(novo);
        log.info("[REGISTRAR_NOVO_STATUS] criado novo registro id={} protocolo={} status='{}' status_id={}",
                novo.getId(), protocolo, novo.getStatus(), novo.getStatusId());

        // Notificar Mosia (o notifierService deve tentar enviar para o MOSIA)
        boolean enviado = false;
        try {
            enviado = notifierService.enviarAtualizacaoParaMosia(protocolo, novoStatus, novo.getChaveUnica());
        } catch (Exception e) {
            log.error("Erro ao notificar Mosia via notifierService: {}", e.getMessage(), e);
            enviado = false;
        }

        // Atualiza flag de sincronização conforme resultado
        novo.setSincronizadoMosia(enviado ? "S" : "N");
        autorizadoAtualizarTimestamp(novo);
        autorizacaoRepo.saveAndFlush(novo);

        if (!enviado) {
            log.warn("[AUTORIZACAO] Falha ao notificar Mosia para protocolo {} status {}", protocolo, novoStatus);
        } else {
            log.info("[AUTORIZACAO] Mosia notificado para protocolo {} com status {}", protocolo, novoStatus);
        }

        return novo;
    }

    // utilitário para acionar PreUpdate (atualiza updatedAt por @PreUpdate, mas
    // aqui forçamos save)
    private void autorizadoAtualizarTimestamp(AutorizacaoSolicitada ent) {
        // método propositalmente simples: valores de createdAt são controlados pelos
        // callbacks do JPA.
        ent.setUpdatedAt(java.time.OffsetDateTime.now());
    }

    /**
     * Mantém compatibilidade: qualquer atualização local agora cria um novo
     * registro
     */
    @Transactional
    public void updateStatusLocal(String protocolo, String novoStatus) {
        registrarNovoStatus(protocolo, novoStatus);
    }
}
