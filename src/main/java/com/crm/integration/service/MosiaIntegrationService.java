package com.crm.integration.service;

import com.crm.integration.model.MosiaStatusUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// @Service
// public class MosiaIntegrationService {

//     @Value("${mosia.api.ocorrencia}")
//     private String mosiaOcorrenciaUrl;

//     @Value("${mosia.token.ocorrencia}")
//     private String mosiaToken;

//     private final RestTemplate restTemplate = new RestTemplate();

//     /**
//      * Envia o PUT para o MOSIA usando exatamente o objeto recebido do cliente (Postman/Controller).
//      */
//     public boolean atualizarStatusNoMosia(String protocolo, MosiaStatusUpdateRequest request) {
//         try {
//             String url = mosiaOcorrenciaUrl + "/" + protocolo + "/status";

//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.APPLICATION_JSON);
//             // usa o token configurado em application.properties (ex: "Bearer xxxxx")
//             headers.set("Authorization", mosiaToken);

//             HttpEntity<MosiaStatusUpdateRequest> entity = new HttpEntity<>(request, headers);

//             ResponseEntity<String> response = restTemplate.exchange(
//                     url,
//                     HttpMethod.PUT,
//                     entity,
//                     String.class
//             );

//             System.out.println("MOSIA -> " + response.getStatusCode() + " : " + response.getBody());
//             return response.getStatusCode().is2xxSuccessful();

//         } catch (Exception e) {
//             System.err.println("Erro ao chamar MOSIA: " + e.getMessage());
//             e.printStackTrace();
//             return false;
//         }
//     }

//     public boolean atualizarStatusNoMosia(String protocolo, String nomeStatus,  String chaveUnica) {
//     try {
//         String url = mosiaOcorrenciaUrl + "/" + protocolo + "/status";

//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         headers.set("Authorization", mosiaToken);

//         // Monta o corpo básico para o MOSIA
//         MosiaStatusUpdateRequest request = new MosiaStatusUpdateRequest();
//         request.setIdNovoStatus(mapearCodigoPorNome(nomeStatus));
//         request.setTextoDesfecho(nomeStatus);
//         request.setProtocolo(protocolo);
//         request.setManterAtribuicao(true);
//         request.setChaveUnicaSolicitante(chaveUnica); 

//         HttpEntity<MosiaStatusUpdateRequest> entity = new HttpEntity<>(request, headers);
//         ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

//         System.out.println("✅ Enviado para MOSIA -> " + response.getStatusCode() + " : " + response.getBody());
//         return response.getStatusCode().is2xxSuccessful();

//     } catch (Exception e) {
//         System.err.println("❌ Erro ao enviar atualização para MOSIA: " + e.getMessage());
//         return false;
//     }
// }

// private Integer mapearCodigoPorNome(String nomeStatus) {
//     switch (nomeStatus) {
//         case "Enviado": return 4112;
//         case "Em Atendimento": return 4110;
//         case "Processado - Consulte Liberações": return 4121;
//         case "Cancelado": return 4122;
//         default: return null;
//     }
// }
// }


@Service
public class MosiaIntegrationService {

    @Value("${mosia.api.ocorrencia}")
    private String mosiaOcorrenciaUrl;

    @Value("${mosia.token.ocorrencia}")
    private String mosiaToken;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean atualizarStatusNoMosia(String protocolo, String nomeStatus, String chaveUnica) {
        try {
            String url = mosiaOcorrenciaUrl + "/" + protocolo + "/status";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", mosiaToken);

            MosiaStatusUpdateRequest request = new MosiaStatusUpdateRequest();
            request.setIdNovoStatus(mapearCodigoPorNome(nomeStatus));
            request.setTextoDesfecho(nomeStatus);
            request.setProtocolo(protocolo);
            request.setChaveUnicaSolicitante(chaveUnica);
            request.setManterAtribuicao(true);

            HttpEntity<MosiaStatusUpdateRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            System.out.println("✅ Enviado para MOSIA -> " + response.getStatusCode() + " : " + response.getBody());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar atualização para MOSIA: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private Integer mapearCodigoPorNome(String nomeStatus) {
        switch (nomeStatus) {
            case "Enviado": return 4112;
            case "Em Atendimento": return 4110;
            case "Processado - Consulte Liberações": return 4121;
            case "Cancelado": return 4122;
            default: return null;
        }
    }
}
