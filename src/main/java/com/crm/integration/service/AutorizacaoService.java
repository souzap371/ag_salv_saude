package com.crm.integration.service;

import com.crm.integration.model.AutorizacaoArquivo;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.repository.AutorizacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AutorizacaoService {

    private final AutorizacaoRepository autorizacaoRepo;
    private final StorageService storageService;
    private final AutorizacaoNotifierService notifierService;

    public AutorizacaoService(AutorizacaoRepository autorizacaoRepo,
                              StorageService storageService,
                              AutorizacaoNotifierService notifierService) {
        this.autorizacaoRepo = autorizacaoRepo;
        this.storageService = storageService;
        this.notifierService = notifierService;
    }

    /**
     * Salva uma nova autorização recebida via callback.
     * Baixa arquivos caso ainda não tenham sido baixados.
     */
    @Transactional
    public void saveAutorizacao(AutorizacaoSolicitada entity) {
        if (entity.getArquivos() != null) {
            for (AutorizacaoArquivo ar : entity.getArquivos()) {
                if (ar.getUrlOriginal() != null && ar.getCaminhoLocal() == null) {
                    String localPath = storageService.downloadAndSave(ar.getUrlOriginal(), "uploads/autorizacoes");
                    ar.setCaminhoLocal(localPath);
                    ar.setBaixadoEm(OffsetDateTime.now());
                    ar.setAutorizacao(entity);
                }
            }
        }
        // Sempre salvar como novo registro
        autorizacaoRepo.save(entity);
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

        // Preenche protocolo e status
        novo.setProtocolo(protocolo);
        novo.setStatus(novoStatus);

        // Persistir no banco
        autorizacaoRepo.save(novo);

        // Notificar Mosia
        boolean enviado = notifierService.enviarAtualizacaoParaMosia(protocolo, novoStatus, novo.getChaveUnica());
        if (!enviado) {
            System.out.println("[AUTORIZACAO] Falha ao notificar Mosia para protocolo " + protocolo);
        } else {
            System.out.println("[AUTORIZACAO] Mosia notificado para protocolo " + protocolo + " com status " + novoStatus);
        }

        return novo;
    }

    /**
     * Mantém compatibilidade: qualquer atualização local agora cria um novo registro
     */
    @Transactional
    public void updateStatusLocal(String protocolo, String novoStatus) {
        registrarNovoStatus(protocolo, novoStatus);
    }
}
