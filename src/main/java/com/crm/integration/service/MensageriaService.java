package com.crm.integration.service;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

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
        entity.setDtInclusao(LocalDateTime.now());
        entity.setIdFuncionalidade(idFuncionalidade);
        entity.setIdRegistro(idRegistro);
        repository.save(entity);

        // Envia para Mosia
        enviarParaMosia(request);

        return entity;
    }

    // Tornar público para o scheduler
    public boolean enviarParaMosia(MensagemRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", mosiaApiToken);

        HttpEntity<MensagemRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(mosiaApiUrl, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Falha ao enviar para Mosia: " + response.getStatusCode());
                return false;
            } else {
                System.out.println("Mensagem enviada para Mosia com sucesso: " + response.getBody());
                return true;
            }
        } catch (Exception e) {
            System.out.println("Erro ao enviar para Mosia: " + e.getMessage());
            return false;
        }
    }
}
