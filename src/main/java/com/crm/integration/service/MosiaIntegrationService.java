package com.crm.integration.service;

import com.crm.integration.model.MosiaStatusUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MosiaIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(MosiaIntegrationService.class);

    @Value("${mosia.api.ocorrencia}")
    private String mosiaOcorrenciaUrl;

    @Value("${mosia.token.ocorrencia}")
    private String mosiaToken;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Atualiza o status da ocorrência no MOSIA.
     * Aceita tanto o ID numérico quanto o nome do status.
     */
    // public boolean atualizarStatusNoMosia(String protocolo, String statusValor,
    // String chaveUnica)
    public boolean atualizarStatusNoMosia(
            Long idOcorrencia,
            String protocolo,
            String statusValor,
            String chaveUnica) {
        try {
            // String url = mosiaOcorrenciaUrl + "/" + protocolo + "/status";
            String url = mosiaOcorrenciaUrl + "/" + idOcorrencia + "/status";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", mosiaToken);

            // tenta converter o status para número; se não conseguir, faz o mapeamento por
            // nome
            Integer idStatus = null;
            if (statusValor != null) {
                try {
                    idStatus = Integer.valueOf(statusValor);
                } catch (NumberFormatException e) {
                    idStatus = mapearCodigoPorNome(statusValor);
                }
            }

            MosiaStatusUpdateRequest request = new MosiaStatusUpdateRequest();
            request.setIdNovoStatus(idStatus);
            request.setTextoDesfecho(statusValor);
            request.setProtocolo(protocolo);
            request.setChaveUnicaSolicitante(chaveUnica);
            request.setManterAtribuicao(true);

            String jsonEnvio = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
            log.info("Enviando atualização para MOSIA: url={} payload={}", url, jsonEnvio);

            HttpEntity<MosiaStatusUpdateRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

            log.info("Retorno MOSIA -> {} : {}", response.getStatusCode(), response.getBody());
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Erro ao enviar atualização para MOSIA: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Faz o mapeamento de nome do status para o ID correspondente no MOSIA.
     * (método privado para uso interno)
     */
    private Integer mapearCodigoPorNome(String nomeStatus) {
        if (nomeStatus == null)
            return null;

        switch (nomeStatus.trim().toUpperCase()) {

            case "ENVIADA":
                return 4682;

            case "SOLICITAÇÃO EM ANÁLISE":
                return 4681;

            default:
                log.warn("Status MOSIA não mapeado: {}", nomeStatus);
                return null;
        }
    }

    /**
     * Método público que expõe o mapeamento nome->id para outros serviços.
     * Útil para preencher statusId antes de persistir.
     */
    public Integer mapearCodigoPorNomePublic(String nomeStatus) {
        return mapearCodigoPorNome(nomeStatus);
    }
}
