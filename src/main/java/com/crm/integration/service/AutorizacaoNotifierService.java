package com.crm.integration.service;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.dto.MosiaRequestDTO.OcorrenciaMosia;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class AutorizacaoNotifierService {

    @Value("${mosia.api.ocorrencia}")
    private String mosiaApiUrl;

    @Value("${mosia.token.ocorrencia}")
    private String mosiaApiToken;

    private final RestTemplate rest;
    private final ObjectMapper objectMapper;

    public AutorizacaoNotifierService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);   // 5s para conectar
        factory.setReadTimeout(7000);      // 7s para receber resposta

        this.rest = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Envia atualização de status da autorização para o Mosia
     */
    public boolean enviarAtualizacaoParaMosia(String protocolo, String status, String chaveUnica) {
        try {
            MosiaRequestDTO dto = criarPayload(protocolo, status, chaveUnica);

            String jsonPayload = objectMapper.writeValueAsString(dto);

            System.out.println("[MOSIA-NOTIFIER] Enviando atualização...");
            System.out.println("[MOSIA-NOTIFIER] URL: " + mosiaApiUrl);
            System.out.println("[MOSIA-NOTIFIER] Payload: " + jsonPayload);
            System.out.println("[MOSIA-NOTIFIER] Token: " + mascararToken(mosiaApiToken));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Garante que sempre tenha "Bearer " antes do token
            String tokenHeader = mosiaApiToken.startsWith("Bearer ") ? mosiaApiToken : "Bearer " + mosiaApiToken;
            headers.set("Authorization", tokenHeader);

            HttpEntity<MosiaRequestDTO> entity = new HttpEntity<>(dto, headers);

            ResponseEntity<String> resp = rest.postForEntity(mosiaApiUrl, entity, String.class);

            System.out.println("[MOSIA-NOTIFIER] Resposta: HTTP " + resp.getStatusCode() + " - " + resp.getBody());

            if (!resp.getStatusCode().is2xxSuccessful()) {
                System.err.println("[MOSIA-NOTIFIER] Erro: Resposta não foi 200. Verificar token, URL ou payload.");
                return false;
            }

            return true;

        } catch (HttpStatusCodeException e) {
            System.err.println("[MOSIA-NOTIFIER] ERRO HTTP: " + e.getStatusCode());
            System.err.println("[MOSIA-NOTIFIER] Corpo da resposta: " + e.getResponseBodyAsString());
            return false;

        } catch (Exception e) {
            System.err.println("[MOSIA-NOTIFIER] Erro inesperado ao enviar atualização para Mosia:");
            e.printStackTrace();
            return false;
        }
    }

    private MosiaRequestDTO criarPayload(String protocolo, String status, String chaveUnica) {
        MosiaRequestDTO dto = new MosiaRequestDTO();
        dto.setInstancia_aplicacao("1");

        OcorrenciaMosia ocorr = new OcorrenciaMosia();
        ocorr.setProtocolo(protocolo);

        MosiaRequestDTO.OcorrenciaMosia.Atendimento at = new MosiaRequestDTO.OcorrenciaMosia.Atendimento();
        at.setStatus(new MosiaRequestDTO.OcorrenciaMosia.Atendimento.Status(status));
        ocorr.setAtendimento(at);

        dto.setOcorrencia(ocorr);

        return dto;
    }

    /**
     * Mascarar token para exibir apenas os 6 primeiros e 4 últimos caracteres
     */
    private String mascararToken(String token) {
        if (token == null || token.length() < 15) return "***INVALID_TOKEN***";

        String clean = token.replace("Bearer ", "");
        return clean.substring(0, 6) + "****" + clean.substring(clean.length() - 4);
    }
}

