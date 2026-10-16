// package com.crm.integration.scheduler;

// import com.crm.integration.model.AutorizacaoSolicitada;
// import com.crm.integration.repository.AutorizacaoSolicitadaRepository;
// import com.crm.integration.service.MosiaIntegrationService;
// import jakarta.transaction.Transactional;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// import java.time.OffsetDateTime;
// import java.time.ZoneId;
// import java.util.List;

// @Component
// public class SincronizacaoMosiaScheduler {

//     private final AutorizacaoSolicitadaRepository repository;
//     private final MosiaIntegrationService mosiaService;

//     public SincronizacaoMosiaScheduler(AutorizacaoSolicitadaRepository repository,
//             MosiaIntegrationService mosiaService) {
//         this.repository = repository;
//         this.mosiaService = mosiaService;
//     }

//     @Scheduled(fixedDelay = 15000) // a cada 15 segundos
//     @Transactional
//     public void sincronizarComMosia() {
//         List<AutorizacaoSolicitada> pendentes = repository.findBySincronizadoMosia("N");

//         System.out.println("[MOSIA SYNC] Registros pendentes encontrados: " + pendentes.size());

//         for (AutorizacaoSolicitada aut : pendentes) {
//             String protocolo = aut.getProtocolo();
//             String status = aut.getStatus();

//             System.out.println("[MOSIA SYNC] Enviando protocolo " + protocolo + " com status " + status);

//             boolean enviado = mosiaService.atualizarStatusNoMosia(
//                     protocolo,
//                     status,
//                     aut.getChaveUnica());

//             if (enviado) {
//                 aut.setSincronizadoMosia("S");
//                 aut.setUltimaSincronizacao(OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")));
//                 repository.save(aut);
//                 System.out.println("[MOSIA SYNC] ✅ Sincronizado com sucesso: " + protocolo);
//             } else {
//                 System.out.println("[MOSIA SYNC] ❌ Falha ao sincronizar protocolo: " + protocolo);
//             }
//         }
//     }
// }

package com.crm.integration.scheduler;

import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.repository.AutorizacaoRepository;
import com.crm.integration.service.MosiaIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@ConditionalOnProperty(name = "features.legacy-autorizacao-sync.enabled", havingValue = "true")
public class SincronizacaoMosiaScheduler {

        private static final Logger log = LoggerFactory.getLogger(
                        SincronizacaoMosiaScheduler.class);

        private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

        private final AutorizacaoRepository repository;
        private final MosiaIntegrationService mosiaService;

        public SincronizacaoMosiaScheduler(
                        AutorizacaoRepository repository,
                        MosiaIntegrationService mosiaService) {

                this.repository = repository;
                this.mosiaService = mosiaService;
        }

        /**
         * Executa 15 segundos após a finalização da execução anterior.
         *
         * Processa apenas o registro mais recente de cada protocolo.
         */
        @Scheduled(fixedDelay = 15000)
        @Transactional
        public void sincronizarComMosia() {

                List<AutorizacaoSolicitada> pendentes = repository.buscarUltimasPendencias();

                if (pendentes.isEmpty()) {
                        log.debug(
                                        "[MOSIA SYNC] Nenhuma pendência encontrada.");

                        return;
                }

                log.info(
                                "[MOSIA SYNC] Pendências mais recentes encontradas: {}",
                                pendentes.size());

                for (AutorizacaoSolicitada autorizacao : pendentes) {

                        processarPendencia(autorizacao);
                }
        }

        private void processarPendencia(
                        AutorizacaoSolicitada autorizacao) {

                String protocolo = normalizar(autorizacao.getProtocolo());

                String status = normalizar(autorizacao.getStatus());

                String chaveUnica = normalizar(autorizacao.getChaveUnica());

                Integer statusId = converterStatusId(
                                autorizacao.getStatusId());

                if (protocolo == null) {
                        log.warn(
                                        "[MOSIA SYNC] Registro ignorado: "
                                                        + "protocolo não informado. id={}",
                                        autorizacao.getId());

                        return;
                }

                if (chaveUnica == null) {
                        log.warn(
                                        "[MOSIA SYNC] Registro ignorado: "
                                                        + "chave única não informada. "
                                                        + "id={} protocolo={}",
                                        autorizacao.getId(),
                                        protocolo);

                        return;
                }

                if (statusId == null
                                && status == null) {

                        log.warn(
                                        "[MOSIA SYNC] Registro ignorado: "
                                                        + "status e statusId ausentes. "
                                                        + "id={} protocolo={}",
                                        autorizacao.getId(),
                                        protocolo);

                        return;
                }

                log.info(
                                "[MOSIA SYNC] Preparando envio. "
                                                + "id={} protocolo={} status={} statusId={}",
                                autorizacao.getId(),
                                protocolo,
                                status,
                                statusId);

                boolean enviado = mosiaService.atualizarStatusNoMosia(
                                protocolo,
                                statusId,
                                status,
                                chaveUnica);

                if (!enviado) {
                        log.warn(
                                        "[MOSIA SYNC] Falha ao sincronizar. "
                                                        + "id={} protocolo={} status={} statusId={}",
                                        autorizacao.getId(),
                                        protocolo,
                                        status,
                                        statusId);

                        return;
                }

                OffsetDateTime agora = OffsetDateTime.now(ZONE_ID);

                /*
                 * O envio foi aceito pela Mosia.
                 * Marca todas as versões pendentes do protocolo como concluídas.
                 */
                int registrosAtualizados = repository
                                .marcarProtocoloComoSincronizado(
                                                protocolo,
                                                agora);

                log.info(
                                "[MOSIA SYNC] Sincronizado com sucesso. "
                                                + "protocolo={} statusId={} registrosAtualizados={}",
                                protocolo,
                                statusId,
                                registrosAtualizados);
        }

        private Integer converterStatusId(
                        String statusIdValor) {

                String valorNormalizado = normalizar(statusIdValor);

                if (valorNormalizado == null) {
                        return null;
                }

                try {
                        return Integer.valueOf(
                                        valorNormalizado);

                } catch (NumberFormatException e) {
                        log.warn(
                                        "[MOSIA SYNC] STATUS_ID inválido: {}",
                                        statusIdValor);

                        return null;
                }
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
