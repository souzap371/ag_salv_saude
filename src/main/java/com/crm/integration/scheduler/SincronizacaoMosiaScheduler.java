package com.crm.integration.scheduler;

import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.repository.AutorizacaoSolicitadaRepository;
import com.crm.integration.service.MosiaIntegrationService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class SincronizacaoMosiaScheduler {

    private final AutorizacaoSolicitadaRepository repository;
    private final MosiaIntegrationService mosiaService;

    public SincronizacaoMosiaScheduler(AutorizacaoSolicitadaRepository repository, MosiaIntegrationService mosiaService) {
        this.repository = repository;
        this.mosiaService = mosiaService;
    }

    @Scheduled(fixedDelay = 15000) // a cada 15 segundos
    @Transactional
    public void sincronizarComMosia() {
        List<AutorizacaoSolicitada> pendentes = repository.findBySincronizadoMosia("N");

        System.out.println("[MOSIA SYNC] Registros pendentes encontrados: " + pendentes.size());

        for (AutorizacaoSolicitada aut : pendentes) {
            String protocolo = aut.getProtocolo();
            String status = aut.getStatus();

            System.out.println("[MOSIA SYNC] Enviando protocolo " + protocolo + " com status " + status);

            boolean enviado = mosiaService.atualizarStatusNoMosia(aut.getMosiaOcorrenciaId(), protocolo, status, aut.getChaveUnica());

            if (enviado) {
                aut.setSincronizadoMosia("S");
                aut.setUltimaSincronizacao(OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")));
                repository.save(aut);
                System.out.println("[MOSIA SYNC] ✅ Sincronizado com sucesso: " + protocolo);
            } else {
                System.out.println("[MOSIA SYNC] ❌ Falha ao sincronizar protocolo: " + protocolo);
            }
        }
    }
}
