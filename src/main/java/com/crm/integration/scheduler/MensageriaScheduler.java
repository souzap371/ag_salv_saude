
package com.crm.integration.scheduler;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import com.crm.integration.service.MensageriaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MensageriaScheduler {

    private final AgMensAppRepository repository;
    private final MensageriaService mensageriaService;

    public MensageriaScheduler(AgMensAppRepository repository, MensageriaService mensageriaService) {
        this.repository = repository;
        this.mensageriaService = mensageriaService;
    }

    // Roda a cada 10 segundos (ajuste conforme necessidade)
    @Scheduled(fixedDelay = 10000)
    public void enviarMensagensPendentes() {
        List<AgMensApp> pendentes = repository.findByDtEnvioIsNull();
        for (AgMensApp msg : pendentes) {
            MensagemRequest request = new MensagemRequest();
            request.setChaveUnica(msg.getChaveUnica());
            request.setMsg(msg.getMensagem());

            boolean enviado = mensageriaService.enviarParaMosia(request);
            if (enviado) {
                msg.setDtEnvio(LocalDateTime.now());
                repository.save(msg);
            }
        }
    }
}
