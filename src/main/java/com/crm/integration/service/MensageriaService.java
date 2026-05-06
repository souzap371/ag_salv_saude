package com.crm.integration.service;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class MensageriaService {

    private final AgMensAppRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mosia.api.url}")
    private String mosiaApiUrl;

    @Value("${mensageria.token}")
    private String mosiaApiToken;

    public MensageriaService(AgMensAppRepository repository) {
        this.repository = repository;
    }

    // Salva no banco e envia imediatamente
    public AgMensApp salvarMensagem(MensagemRequest request, String idFuncionalidade, String idRegistro) {
        AgMensApp entity = new AgMensApp();
        entity.setChaveUnica(request.getChaveUnica());
        entity.setMensagem(request.getMsg());
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        entity.setDtInclusao(LocalDateTime.now(zone));
        entity.setIdFuncionalidade(idFuncionalidade);
        entity.setIdRegistro(idRegistro);
        repository.save(entity);

        // Envia para Mosia
        enviarParaMosia(request);

        return entity;
    }

    public boolean enviarParaMosia(MensagemRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", mosiaApiToken);

        HttpEntity<MensagemRequest> entity = new HttpEntity<>(request, headers);

        try {
            System.out.println("[SERVICE] Enviando para Mosia - Chave: " + request.getChaveUnica() +
                    " | Msg: " + request.getMsg());

            ResponseEntity<String> response = restTemplate.postForEntity(mosiaApiUrl, entity, String.class);

            System.out.println("[SERVICE] Resposta Mosia: HTTP " + response.getStatusCode() +
                    " | Body: " + response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                System.out.println("[SERVICE] Falha ao enviar para Mosia: " + response.getStatusCode());
                return false;
            } else {
                System.out.println("[SERVICE] Mensagem enviada com sucesso.");
                return true;
            }
        } catch (Exception e) {
            System.out.println("[SERVICE] Erro ao enviar para Mosia: " + e.getMessage());
            return false;
        }
    }

}

