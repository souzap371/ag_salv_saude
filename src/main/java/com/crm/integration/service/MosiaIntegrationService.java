package com.crm.integration.service;


import com.crm.integration.model.MosiaStatusUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MosiaIntegrationService {

    @Value("${mosia.api.ocorrencia}")
    private String mosiaOcorrenciaUrl;

    @Value("${mosia.token.ocorrencia}")
    private String mosiaToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean atualizarStatusNoMosia(String protocolo, String novoStatus) {
        try {
            String url = mosiaOcorrenciaUrl + "/atualizar-status";

            MosiaStatusUpdateRequest body = new MosiaStatusUpdateRequest(protocolo, novoStatus);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", mosiaToken);

            HttpEntity<MosiaStatusUpdateRequest> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
