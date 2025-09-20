package com.crm.integration.service;

import com.crm.integration.model.AutorizacaoArquivo;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.repository.AutorizacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class AutorizacaoService {

    private final AutorizacaoRepository autorizacaoRepo;
    private final StorageService storageService;

    public AutorizacaoService(AutorizacaoRepository autorizacaoRepo,
            StorageService storageService) {
        this.autorizacaoRepo = autorizacaoRepo;
        this.storageService = storageService;
    }

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

        // Se já existir registro com o mesmo protocolo, atualiza
        Optional<AutorizacaoSolicitada> exist = autorizacaoRepo.findByProtocolo(entity.getProtocolo());
        if (exist.isPresent()) {
            AutorizacaoSolicitada old = exist.get();
            entity.setId(old.getId());
        }

        autorizacaoRepo.save(entity);
    }

    public Optional<AutorizacaoSolicitada> findByProtocolo(String protocolo) {
        return autorizacaoRepo.findByProtocolo(protocolo);
    }

    @Transactional
    public void updateStatusLocal(String protocolo, String novoStatus) {
        autorizacaoRepo.findByProtocolo(protocolo).ifPresent(ent -> {
            ent.setStatus(novoStatus);
            autorizacaoRepo.save(ent);
        });
    }
}
