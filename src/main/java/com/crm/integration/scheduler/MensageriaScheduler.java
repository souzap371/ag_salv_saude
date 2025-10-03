
package com.crm.integration.scheduler;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import com.crm.integration.service.MensageriaService;

import jakarta.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class MensageriaScheduler {

    private final AgMensAppRepository repository;
    private final MensageriaService mensageriaService;

    public MensageriaScheduler(AgMensAppRepository repository, MensageriaService mensageriaService) {
        this.repository = repository;
        this.mensageriaService = mensageriaService;
    }

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void enviarMensagensPendentes() {
        List<AgMensApp> pendentes = repository.findByDtEnvioIsNull();

        System.out.println("[SCHEDULER] Mensagens pendentes encontradas: " + pendentes.size());

        for (AgMensApp msg : pendentes) {
            System.out.println("[SCHEDULER] Preparando envio - ID: " + msg.getId() +
                    " | Chave: " + msg.getChaveUnica() +
                    " | Mensagem: " + msg.getMensagem());

            MensagemRequest request = new MensagemRequest();
            request.setChaveUnica(msg.getChaveUnica());
            request.setMsg(msg.getMensagem());

            boolean enviado = mensageriaService.enviarParaMosia(request);

            if (enviado) {
                ZoneId zone = ZoneId.of("America/Sao_Paulo");
                msg.setDtEnvio(LocalDateTime.now(zone));
                repository.save(msg);
                System.out.println("[SCHEDULER] Mensagem ID " + msg.getId() + " enviada e marcada como enviada.");
            } else {
                System.out.println("[SCHEDULER] Falha ao enviar mensagem ID " + msg.getId());
            }
        }
    }

}
