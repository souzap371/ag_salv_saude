// package com.crm.integration.scheduler;

// import com.crm.integration.model.AgMensApp;
// import com.crm.integration.model.MensagemRequest;
// import com.crm.integration.repository.AgMensAppRepository;
// import com.crm.integration.service.MensageriaService;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.List;

// @Component
// public class MensageriaScheduler {

//     private static final ZoneId ZONE_ID =
//             ZoneId.of("America/Sao_Paulo");

//     private static final int DIAS_LIMITE = 2;
//     private static final int TAMANHO_LOTE = 100;

//     private final AgMensAppRepository repository;
//     private final MensageriaService mensageriaService;

//     public MensageriaScheduler(
//             AgMensAppRepository repository,
//             MensageriaService mensageriaService
//     ) {
//         this.repository = repository;
//         this.mensageriaService = mensageriaService;
//     }

//     @Scheduled(
//             fixedDelayString =
//                     "${mensageria.scheduler.fixed-delay:10000}",
//             initialDelayString =
//                     "${mensageria.scheduler.initial-delay:10000}"
//     )
//     public void enviarMensagensPendentes() {

//         LocalDateTime dataLimite = LocalDate.now(ZONE_ID)
//                 .minusDays(DIAS_LIMITE)
//                 .atStartOfDay();

//         Pageable pageable = PageRequest.of(
//                 0,
//                 TAMANHO_LOTE
//         );

//         List<AgMensApp> pendentes;

//         try {
//             pendentes = repository
//                     .findByDtEnvioIsNullAndDtInclusaoIsNotNullAndDtInclusaoGreaterThanEqualAndChaveUnicaIsNotNullOrderByDtInclusaoAsc(
//                             dataLimite,
//                             pageable
//                     );
//         } catch (Exception e) {
//             System.out.println(
//                     "[SCHEDULER] Erro ao consultar mensagens pendentes: "
//                             + e.getMessage()
//             );
//             return;
//         }

//         if (pendentes.isEmpty()) {
//             System.out.println(
//                     "[SCHEDULER] Nenhuma mensagem pendente desde "
//                             + dataLimite
//             );
//             return;
//         }

//         System.out.println(
//                 "[SCHEDULER] Data limite: " + dataLimite
//                         + " | Lote encontrado: " + pendentes.size()
//         );

//         int enviadas = 0;
//         int falhas = 0;
//         int ignoradas = 0;

//         for (AgMensApp mensagem : pendentes) {
//             try {
//                 ResultadoProcessamento resultado =
//                         processarMensagem(mensagem);

//                 switch (resultado) {
//                     case ENVIADA -> enviadas++;
//                     case FALHA -> falhas++;
//                     case IGNORADA -> ignoradas++;
//                 }
//             } catch (Exception e) {
//                 falhas++;

//                 System.out.println(
//                         "[SCHEDULER] Erro inesperado na mensagem ID "
//                                 + mensagem.getId()
//                                 + ": "
//                                 + e.getMessage()
//                 );
//             }
//         }

//         System.out.println(
//                 "[SCHEDULER] Lote finalizado"
//                         + " | Enviadas: " + enviadas
//                         + " | Falhas: " + falhas
//                         + " | Ignoradas: " + ignoradas
//         );
//     }

//     private ResultadoProcessamento processarMensagem(
//             AgMensApp mensagem
//     ) {
//         if (mensagem == null || mensagem.getId() == null) {
//             System.out.println(
//                     "[SCHEDULER] Registro inválido ignorado."
//             );
//             return ResultadoProcessamento.IGNORADA;
//         }

//         /*
//          * Evita processar novamente um objeto que já esteja marcado
//          * como enviado na memória.
//          */
//         if (mensagem.getDtEnvio() != null) {
//             System.out.println(
//                     "[SCHEDULER] Mensagem já enviada. ID: "
//                             + mensagem.getId()
//             );
//             return ResultadoProcessamento.IGNORADA;
//         }

//         if (mensagem.getChaveUnica() == null
//                 || mensagem.getChaveUnica().isBlank()) {

//             System.out.println(
//                     "[SCHEDULER] Mensagem ignorada por chave inválida. ID: "
//                             + mensagem.getId()
//             );
//             return ResultadoProcessamento.IGNORADA;
//         }

//         if (mensagem.getMensagem() == null
//                 || mensagem.getMensagem().isBlank()) {

//             System.out.println(
//                     "[SCHEDULER] Mensagem ignorada por conteúdo vazio. ID: "
//                             + mensagem.getId()
//             );
//             return ResultadoProcessamento.IGNORADA;
//         }

//         System.out.println(
//                 "[SCHEDULER] Preparando envio"
//                         + " | ID: " + mensagem.getId()
//                         + " | Chave: " + mensagem.getChaveUnica()
//                         + " | Inclusão: " + mensagem.getDtInclusao()
//         );

//         MensagemRequest request = new MensagemRequest();
//         request.setChaveUnica(mensagem.getChaveUnica());
//         request.setMsg(mensagem.getMensagem());

//         boolean enviado =
//                 mensageriaService.enviarParaMosia(request);

//         if (!enviado) {
//             System.out.println(
//                     "[SCHEDULER] Falha ao enviar mensagem ID "
//                             + mensagem.getId()
//             );
//             return ResultadoProcessamento.FALHA;
//         }

//         mensagem.setDtEnvio(LocalDateTime.now(ZONE_ID));
//         mensagem.setProc("S");

//         try {
//             repository.save(mensagem);

//             System.out.println(
//                     "[SCHEDULER] Mensagem ID "
//                             + mensagem.getId()
//                             + " enviada e marcada como enviada."
//             );

//             return ResultadoProcessamento.ENVIADA;

//         } catch (Exception e) {
//             /*
//              * Atenção: nesse cenário a API pode ter recebido a mensagem,
//              * mas o banco não conseguiu registrar o envio.
//              */
//             System.out.println(
//                     "[SCHEDULER] Mensagem enviada para a Mosia, "
//                             + "mas houve erro ao atualizar o banco. ID: "
//                             + mensagem.getId()
//                             + " | Erro: "
//                             + e.getMessage()
//             );

//             return ResultadoProcessamento.FALHA;
//         }
//     }

//     private enum ResultadoProcessamento {
//         ENVIADA,
//         FALHA,
//         IGNORADA
//     }
// }

package com.crm.integration.scheduler;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import com.crm.integration.service.MensageriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MensageriaScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(MensageriaScheduler.class);

    private static final ZoneId ZONE_ID =
            ZoneId.of("America/Sao_Paulo");

    /*
     * Status utilizados pela tabela AG_MENSAPP.
     *
     * N = Não processado / pendente de envio
     * S = Processado / enviado com sucesso
     */
    private static final String PROC_NAO_PROCESSADO = "N";
    private static final String PROC_PROCESSADO = "S";

    private final AgMensAppRepository repository;
    private final MensageriaService mensageriaService;

    /*
     * Proteção em memória contra processamento simultâneo
     * da mesma mensagem dentro desta instância da aplicação.
     *
     * Quando um ID entra em processamento, ele é adicionado
     * neste Set.
     *
     * Enquanto permanecer aqui, uma segunda tentativa simultânea
     * para o mesmo ID será ignorada.
     */
    private final Set<Long> mensagensEmProcessamento =
            ConcurrentHashMap.newKeySet();

    /*
     * Quantidade máxima de mensagens processadas
     * em cada execução do scheduler.
     *
     * Caso a propriedade não esteja configurada,
     * será utilizado o valor padrão 100.
     */
    @Value("${mensageria.scheduler.batch-size:100}")
    private int tamanhoLote;

    public MensageriaScheduler(
            AgMensAppRepository repository,
            MensageriaService mensageriaService
    ) {
        this.repository = repository;
        this.mensageriaService = mensageriaService;
    }

    /**
     * Scheduler responsável pelo processamento das mensagens
     * pendentes da tabela AG_MENSAPP.
     *
     * REGRAS:
     *
     * 1 - Somente registros com PROC = 'N'
     * 2 - Somente registros com DTENVIO IS NULL
     * 3 - Somente registros incluídos a partir do dia atual
     * 4 - Somente registros com DTINCLUSAO preenchida
     * 5 - Somente registros com CHAVE_UNICA preenchida
     * 6 - Processamento realizado em lote
     * 7 - Processamento das mensagens mais antigas primeiro
     * 8 - PROC somente muda para 'S' após sucesso confirmado pela Mosia
     * 9 - DTENVIO somente é preenchido após sucesso confirmado pela Mosia
     */
    @Scheduled(
            fixedDelayString =
                    "${mensageria.scheduler.fixed-delay:10000}",
            initialDelayString =
                    "${mensageria.scheduler.initial-delay:10000}"
    )
    public void enviarMensagensPendentes() {

        /*
         * Considera somente mensagens incluídas
         * a partir do início do dia atual.
         *
         * Exemplo:
         *
         * Hoje: 27/08/2026
         *
         * Data inicial:
         * 27/08/2026 00:00:00
         */
        LocalDateTime inicioDoDia =
                LocalDate.now(ZONE_ID)
                        .atStartOfDay();

        /*
         * Página zero contendo no máximo a quantidade
         * definida em mensageria.scheduler.batch-size.
         */
        Pageable pageable =
                PageRequest.of(
                        0,
                        tamanhoLote
                );

        List<AgMensApp> pendentes;

        /*
         * Consulta das mensagens pendentes.
         */
        try {

            pendentes = repository
                    .findByProcAndDtEnvioIsNullAndDtInclusaoIsNotNullAndDtInclusaoGreaterThanEqualAndChaveUnicaIsNotNullOrderByDtInclusaoAsc(
                            PROC_NAO_PROCESSADO,
                            inicioDoDia,
                            pageable
                    );

        } catch (Exception e) {

            log.error(
                    "[SCHEDULER] Erro ao consultar mensagens pendentes.",
                    e
            );

            return;
        }

        /*
         * Nenhuma mensagem encontrada.
         */
        if (pendentes.isEmpty()) {

            log.debug(
                    "[SCHEDULER] Nenhuma mensagem pendente encontrada a partir de {}.",
                    inicioDoDia
            );

            return;
        }

        log.info(
                "[SCHEDULER] Iniciando lote de mensageria. "
                        + "Data inicial: {} | Quantidade: {} | Tamanho máximo do lote: {}",
                inicioDoDia,
                pendentes.size(),
                tamanhoLote
        );

        int enviadas = 0;
        int falhas = 0;
        int ignoradas = 0;

        /*
         * Processamento individual das mensagens.
         */
        for (AgMensApp mensagem : pendentes) {

            try {

                ResultadoProcessamento resultado =
                        processarMensagem(mensagem);

                switch (resultado) {

                    case ENVIADA:
                        enviadas++;
                        break;

                    case FALHA:
                        falhas++;
                        break;

                    case IGNORADA:
                        ignoradas++;
                        break;
                }

            } catch (Exception e) {

                falhas++;

                log.error(
                        "[SCHEDULER] Erro inesperado durante o processamento da mensagem ID {}.",
                        mensagem != null
                                ? mensagem.getId()
                                : null,
                        e
                );
            }
        }

        /*
         * Resumo da execução.
         */
        log.info(
                "[SCHEDULER] Lote finalizado | "
                        + "Enviadas: {} | Falhas: {} | Ignoradas: {}",
                enviadas,
                falhas,
                ignoradas
        );
    }

    /**
     * Processa individualmente uma mensagem da AG_MENSAPP.
     */
    private ResultadoProcessamento processarMensagem(
            AgMensApp mensagem
    ) {

        /*
         * ==========================================================
         * VALIDAÇÃO 1
         * Registro e ID obrigatórios
         * ==========================================================
         */
        if (mensagem == null
                || mensagem.getId() == null) {

            log.warn(
                    "[SCHEDULER] Registro inválido encontrado. Mensagem ignorada."
            );

            return ResultadoProcessamento.IGNORADA;
        }

        Long mensagemId =
                mensagem.getId();

        /*
         * ==========================================================
         * VALIDAÇÃO 2
         * Proteção contra processamento simultâneo
         * ==========================================================
         *
         * add() retorna false caso o ID já esteja presente no Set.
         *
         * Isso evita duas threads desta mesma aplicação
         * enviarem a mesma mensagem simultaneamente.
         */
        if (!mensagensEmProcessamento.add(mensagemId)) {

            log.warn(
                    "[SCHEDULER] Mensagem ID {} já está sendo processada. "
                            + "Nova tentativa simultânea será ignorada.",
                    mensagemId
            );

            return ResultadoProcessamento.IGNORADA;
        }

        try {

            /*
             * ==========================================================
             * VALIDAÇÃO 3
             * Reconsulta o registro antes do envio
             * ==========================================================
             *
             * O registro é consultado novamente no banco para evitar
             * utilizar um objeto que possa ter ficado desatualizado
             * enquanto aguardava sua vez no lote.
             */
            Optional<AgMensApp> mensagemAtualOptional =
                    repository.findById(mensagemId);

            if (mensagemAtualOptional.isEmpty()) {

                log.warn(
                        "[SCHEDULER] Mensagem ID {} não foi encontrada no banco. "
                                + "Mensagem ignorada.",
                        mensagemId
                );

                return ResultadoProcessamento.IGNORADA;
            }

            AgMensApp mensagemAtual =
                    mensagemAtualOptional.get();

            /*
             * ==========================================================
             * VALIDAÇÃO 4
             * PROC já processado
             * ==========================================================
             *
             * Se PROC já estiver como S, a mensagem não pode
             * ser enviada novamente.
             */
            if (PROC_PROCESSADO.equalsIgnoreCase(
                    mensagemAtual.getProc()
            )) {

                log.info(
                        "[SCHEDULER] Mensagem ID {} já possui PROC = 'S'. "
                                + "Não será enviada novamente.",
                        mensagemId
                );

                return ResultadoProcessamento.IGNORADA;
            }

            /*
             * ==========================================================
             * VALIDAÇÃO 5
             * DTENVIO já preenchido
             * ==========================================================
             *
             * Mesmo que PROC esteja inconsistente, uma mensagem
             * que já possui DTENVIO não será reenviada.
             */
            if (mensagemAtual.getDtEnvio() != null) {

                log.info(
                        "[SCHEDULER] Mensagem ID {} já possui DTENVIO = {}. "
                                + "Não será enviada novamente.",
                        mensagemId,
                        mensagemAtual.getDtEnvio()
                );

                return ResultadoProcessamento.IGNORADA;
            }

            /*
             * ==========================================================
             * VALIDAÇÃO 6
             * PROC obrigatoriamente precisa ser N
             * ==========================================================
             */
            if (!PROC_NAO_PROCESSADO.equalsIgnoreCase(
                    mensagemAtual.getProc()
            )) {

                log.warn(
                        "[SCHEDULER] Mensagem ID {} ignorada. "
                                + "PROC atual: {}.",
                        mensagemId,
                        mensagemAtual.getProc()
                );

                return ResultadoProcessamento.IGNORADA;
            }

            /*
             * ==========================================================
             * VALIDAÇÃO 7
             * CHAVE_UNICA obrigatória
             * ==========================================================
             */
            if (mensagemAtual.getChaveUnica() == null
                    || mensagemAtual.getChaveUnica().isBlank()) {

                log.warn(
                        "[SCHEDULER] Mensagem ID {} ignorada porque "
                                + "CHAVE_UNICA está vazia.",
                        mensagemId
                );

                return ResultadoProcessamento.IGNORADA;
            }

            /*
             * ==========================================================
             * VALIDAÇÃO 8
             * Conteúdo da mensagem obrigatório
             * ==========================================================
             */
            if (mensagemAtual.getMensagem() == null
                    || mensagemAtual.getMensagem().isBlank()) {

                log.warn(
                        "[SCHEDULER] Mensagem ID {} ignorada porque "
                                + "o conteúdo da mensagem está vazio.",
                        mensagemId
                );

                return ResultadoProcessamento.IGNORADA;
            }

            /*
             * ==========================================================
             * VALIDAÇÃO 9
             * DTINCLUSAO precisa pertencer ao dia atual
             * ==========================================================
             *
             * Essa validação é realizada novamente imediatamente
             * antes do envio.
             */
            LocalDateTime inicioDoDia =
                    LocalDate.now(ZONE_ID)
                            .atStartOfDay();

            if (mensagemAtual.getDtInclusao() == null
                    || mensagemAtual
                    .getDtInclusao()
                    .isBefore(inicioDoDia)) {

                log.warn(
                        "[SCHEDULER] Mensagem ID {} ignorada. "
                                + "DTINCLUSAO {} é anterior ao início do dia {}.",
                        mensagemId,
                        mensagemAtual.getDtInclusao(),
                        inicioDoDia
                );

                return ResultadoProcessamento.IGNORADA;
            }

            /*
             * ==========================================================
             * PREPARAÇÃO DO ENVIO
             * ==========================================================
             */
            log.info(
                    "[SCHEDULER] Preparando envio | "
                            + "ID: {} | Chave: {} | Inclusão: {}",
                    mensagemId,
                    mensagemAtual.getChaveUnica(),
                    mensagemAtual.getDtInclusao()
            );

            MensagemRequest request =
                    new MensagemRequest();

            request.setChaveUnica(
                    mensagemAtual.getChaveUnica()
            );

            request.setMsg(
                    mensagemAtual.getMensagem()
            );

            boolean enviado;

            /*
             * ==========================================================
             * ENVIO PARA MOSIA
             * ==========================================================
             */
            try {

                enviado =
                        mensageriaService
                                .enviarParaMosia(request);

            } catch (Exception e) {

                /*
                 * Em caso de falha:
                 *
                 * PROC continua N
                 * DTENVIO continua NULL
                 *
                 * A mensagem poderá ser processada novamente
                 * em uma próxima execução enquanto estiver
                 * dentro da janela permitida.
                 */
                log.error(
                        "[SCHEDULER] Falha na comunicação com a Mosia. "
                                + "Mensagem ID {} permanecerá com PROC = 'N' "
                                + "e DTENVIO = NULL.",
                        mensagemId,
                        e
                );

                return ResultadoProcessamento.FALHA;
            }

            /*
             * ==========================================================
             * MOSIA NÃO CONFIRMOU O ENVIO
             * ==========================================================
             */
            if (!enviado) {

                log.error(
                        "[SCHEDULER] Mosia não confirmou o envio da mensagem ID {}. "
                                + "PROC permanecerá 'N' e DTENVIO permanecerá NULL.",
                        mensagemId
                );

                return ResultadoProcessamento.FALHA;
            }

            /*
             * ==========================================================
             * MOSIA CONFIRMOU O ENVIO
             * ==========================================================
             *
             * Somente após confirmação:
             *
             * PROC    = S
             * DTENVIO = data/hora atual
             */
            mensagemAtual.setProc(
                    PROC_PROCESSADO
            );

            mensagemAtual.setDtEnvio(
                    LocalDateTime.now(ZONE_ID)
            );

            /*
             * ==========================================================
             * ATUALIZA AG_MENSAPP
             * ==========================================================
             */
            try {

                repository.save(mensagemAtual);

                log.info(
                        "[SCHEDULER] Mensagem enviada com sucesso. "
                                + "ID: {} | PROC: S | DTENVIO: {}",
                        mensagemId,
                        mensagemAtual.getDtEnvio()
                );

                return ResultadoProcessamento.ENVIADA;

            } catch (Exception e) {

                /*
                 * SITUAÇÃO CRÍTICA:
                 *
                 * É possível que a Mosia tenha recebido a mensagem,
                 * mas o banco tenha falhado na atualização.
                 *
                 * Neste caso PROC poderá continuar N no banco.
                 *
                 * Por isso CHAVE_UNICA deve ser utilizada como
                 * mecanismo adicional de identificação/idempotência.
                 */
                log.error(
                        "[SCHEDULER] CRÍTICO: Mensagem ID {} foi aceita pela Mosia, "
                                + "mas ocorreu erro ao atualizar PROC/DTENVIO no banco.",
                        mensagemId,
                        e
                );

                return ResultadoProcessamento.FALHA;
            }

        } finally {

            /*
             * Sempre remove o ID da lista de mensagens
             * atualmente em processamento.
             */
            mensagensEmProcessamento.remove(
                    mensagemId
            );
        }
    }

    /**
     * Resultado possível para o processamento
     * individual de uma mensagem.
     */
    private enum ResultadoProcessamento {

        ENVIADA,
        FALHA,
        IGNORADA

    }
}