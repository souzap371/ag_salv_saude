package com.crm.integration.service;

import com.crm.integration.model.MosiaStatusUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Atualiza o status da ocorrência no MOSIA
     * Aceita tanto o ID numérico quanto o nome do status.
     */
    public boolean atualizarStatusNoMosia(String protocolo, String statusValor, String chaveUnica) {
        try {
            String url = mosiaOcorrenciaUrl + "/" + protocolo + "/status";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", mosiaToken);

            // 🔹 Tenta converter o status para número; se não conseguir, faz o mapeamento por nome
            Integer idStatus;
            try {
                idStatus = Integer.valueOf(statusValor);
            } catch (NumberFormatException e) {
                idStatus = mapearCodigoPorNome(statusValor);
            }

            MosiaStatusUpdateRequest request = new MosiaStatusUpdateRequest();
            request.setIdNovoStatus(idStatus);
            request.setTextoDesfecho(statusValor);
            request.setProtocolo(protocolo);
            request.setChaveUnicaSolicitante(chaveUnica);
            request.setManterAtribuicao(true);

            // 🔍 Log detalhado do JSON que será enviado
            String jsonEnvio = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            System.out.println("📤 Enviando atualização para MOSIA:");
            System.out.println(jsonEnvio);

            HttpEntity<MosiaStatusUpdateRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            System.out.println("✅ Retorno MOSIA -> " + response.getStatusCode() + " : " + response.getBody());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar atualização para MOSIA: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Faz o mapeamento de nome do status para o ID correspondente no MOSIA.
     */
    private Integer mapearCodigoPorNome(String nomeStatus) {
        if (nomeStatus == null) return null;

        switch (nomeStatus.trim()) {
            case "Enviado":
                return 4112;
            case "Em Atendimento":
                return 4110;
            case "Processado - Consulte Liberações":
                return 4121;
            case "Cancelado":
                return 4122;
            default:
                System.out.println("⚠️ Status não mapeado: " + nomeStatus);
                return null;
        }
    }
}
