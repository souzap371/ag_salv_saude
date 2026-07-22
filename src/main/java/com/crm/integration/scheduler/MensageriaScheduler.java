
// package com.crm.integration.scheduler;

// import com.crm.integration.model.AgMensApp;
// import com.crm.integration.model.MensagemRequest;
// import com.crm.integration.repository.AgMensAppRepository;
// import com.crm.integration.service.MensageriaService;

// import jakarta.transaction.Transactional;

// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.List;

// @Component
// public class MensageriaScheduler {

//     private final AgMensAppRepository repository;
//     private final MensageriaService mensageriaService;

//     public MensageriaScheduler(AgMensAppRepository repository, MensageriaService mensageriaService) {
//         this.repository = repository;
//         this.mensageriaService = mensageriaService;
//     }

//     @Scheduled(fixedDelay = 10000)
//     @Transactional
//     public void enviarMensagensPendentes() {
//         List<AgMensApp> pendentes = repository.findByDtEnvioIsNull();

//         System.out.println("[SCHEDULER] Mensagens pendentes encontradas: " + pendentes.size());

//         for (AgMensApp msg : pendentes) {
//             System.out.println("[SCHEDULER] Preparando envio - ID: " + msg.getId() +
//                     " | Chave: " + msg.getChaveUnica() +
//                     " | Mensagem: " + msg.getMensagem());

//             MensagemRequest request = new MensagemRequest();
//             request.setChaveUnica(msg.getChaveUnica());
//             request.setMsg(msg.getMensagem());

//             boolean enviado = mensageriaService.enviarParaMosia(request);

//             if (enviado) {
//                 ZoneId zone = ZoneId.of("America/Sao_Paulo");
//                 msg.setDtEnvio(LocalDateTime.now(zone));
//                 repository.save(msg);
//                 System.out.println("[SCHEDULER] Mensagem ID " + msg.getId() + " enviada e marcada como enviada.");
//             } else {
//                 System.out.println("[SCHEDULER] Falha ao enviar mensagem ID " + msg.getId());
//             }
//         }
//     }

// }

package com.crm.integration.scheduler;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import com.crm.integration.service.MensageriaService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class MensageriaScheduler {

    private static final ZoneId ZONE_ID =
            ZoneId.of("America/Sao_Paulo");

    private static final int DIAS_LIMITE = 2;
    private static final int TAMANHO_LOTE = 100;

    private final AgMensAppRepository repository;
    private final MensageriaService mensageriaService;

    public MensageriaScheduler(
            AgMensAppRepository repository,
            MensageriaService mensageriaService
    ) {
        this.repository = repository;
        this.mensageriaService = mensageriaService;
    }

    @Scheduled(
            fixedDelayString =
                    "${mensageria.scheduler.fixed-delay:10000}",
            initialDelayString =
                    "${mensageria.scheduler.initial-delay:10000}"
    )
    public void enviarMensagensPendentes() {

        LocalDateTime dataLimite = LocalDate.now(ZONE_ID)
                .minusDays(DIAS_LIMITE)
                .atStartOfDay();

        Pageable pageable = PageRequest.of(
                0,
                TAMANHO_LOTE
        );

        List<AgMensApp> pendentes;

        try {
            pendentes = repository
                    .findByDtEnvioIsNullAndDtInclusaoIsNotNullAndDtInclusaoGreaterThanEqualAndChaveUnicaIsNotNullOrderByDtInclusaoAsc(
                            dataLimite,
                            pageable
                    );
        } catch (Exception e) {
            System.out.println(
                    "[SCHEDULER] Erro ao consultar mensagens pendentes: "
                            + e.getMessage()
            );
            return;
        }

        if (pendentes.isEmpty()) {
            System.out.println(
                    "[SCHEDULER] Nenhuma mensagem pendente desde "
                            + dataLimite
            );
            return;
        }

        System.out.println(
                "[SCHEDULER] Data limite: " + dataLimite
                        + " | Lote encontrado: " + pendentes.size()
        );

        int enviadas = 0;
        int falhas = 0;
        int ignoradas = 0;

        for (AgMensApp mensagem : pendentes) {
            try {
                ResultadoProcessamento resultado =
                        processarMensagem(mensagem);

                switch (resultado) {
                    case ENVIADA -> enviadas++;
                    case FALHA -> falhas++;
                    case IGNORADA -> ignoradas++;
                }
            } catch (Exception e) {
                falhas++;

                System.out.println(
                        "[SCHEDULER] Erro inesperado na mensagem ID "
                                + mensagem.getId()
                                + ": "
                                + e.getMessage()
                );
            }
        }

        System.out.println(
                "[SCHEDULER] Lote finalizado"
                        + " | Enviadas: " + enviadas
                        + " | Falhas: " + falhas
                        + " | Ignoradas: " + ignoradas
        );
    }

    private ResultadoProcessamento processarMensagem(
            AgMensApp mensagem
    ) {
        if (mensagem == null || mensagem.getId() == null) {
            System.out.println(
                    "[SCHEDULER] Registro inválido ignorado."
            );
            return ResultadoProcessamento.IGNORADA;
        }

        /*
         * Evita processar novamente um objeto que já esteja marcado
         * como enviado na memória.
         */
        if (mensagem.getDtEnvio() != null) {
            System.out.println(
                    "[SCHEDULER] Mensagem já enviada. ID: "
                            + mensagem.getId()
            );
            return ResultadoProcessamento.IGNORADA;
        }

        if (mensagem.getChaveUnica() == null
                || mensagem.getChaveUnica().isBlank()) {

            System.out.println(
                    "[SCHEDULER] Mensagem ignorada por chave inválida. ID: "
                            + mensagem.getId()
            );
            return ResultadoProcessamento.IGNORADA;
        }

        if (mensagem.getMensagem() == null
                || mensagem.getMensagem().isBlank()) {

            System.out.println(
                    "[SCHEDULER] Mensagem ignorada por conteúdo vazio. ID: "
                            + mensagem.getId()
            );
            return ResultadoProcessamento.IGNORADA;
        }

        System.out.println(
                "[SCHEDULER] Preparando envio"
                        + " | ID: " + mensagem.getId()
                        + " | Chave: " + mensagem.getChaveUnica()
                        + " | Inclusão: " + mensagem.getDtInclusao()
        );

        MensagemRequest request = new MensagemRequest();
        request.setChaveUnica(mensagem.getChaveUnica());
        request.setMsg(mensagem.getMensagem());

        boolean enviado =
                mensageriaService.enviarParaMosia(request);

        if (!enviado) {
            System.out.println(
                    "[SCHEDULER] Falha ao enviar mensagem ID "
                            + mensagem.getId()
            );
            return ResultadoProcessamento.FALHA;
        }

        mensagem.setDtEnvio(LocalDateTime.now(ZONE_ID));
        mensagem.setProc("S");

        try {
            repository.save(mensagem);

            System.out.println(
                    "[SCHEDULER] Mensagem ID "
                            + mensagem.getId()
                            + " enviada e marcada como enviada."
            );

            return ResultadoProcessamento.ENVIADA;

        } catch (Exception e) {
            /*
             * Atenção: nesse cenário a API pode ter recebido a mensagem,
             * mas o banco não conseguiu registrar o envio.
             */
            System.out.println(
                    "[SCHEDULER] Mensagem enviada para a Mosia, "
                            + "mas houve erro ao atualizar o banco. ID: "
                            + mensagem.getId()
                            + " | Erro: "
                            + e.getMessage()
            );

            return ResultadoProcessamento.FALHA;
        }
    }

    private enum ResultadoProcessamento {
        ENVIADA,
        FALHA,
        IGNORADA
    }
}