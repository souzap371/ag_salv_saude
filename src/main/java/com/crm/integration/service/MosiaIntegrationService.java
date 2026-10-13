// package com.crm.integration.service;

// import com.crm.integration.model.MosiaStatusUpdateRequest;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.*;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// @Service
// public class MosiaIntegrationService {

//     private static final Logger log = LoggerFactory.getLogger(MosiaIntegrationService.class);

//     @Value("${mosia.api.ocorrencia}")
//     private String mosiaOcorrenciaUrl;

//     @Value("${mosia.token.ocorrencia}")
//     private String mosiaToken;

//     private final RestTemplate restTemplate = new RestTemplate();
//     private final ObjectMapper mapper = new ObjectMapper();

//     public boolean atualizarStatusNoMosia(
//             String protocolo,
//             String statusValor,
//             String chaveUnica) {

//         try {
//             if (protocolo == null || protocolo.isBlank()) {
//                 log.warn("Não foi possível atualizar o MOSIA: protocolo não informado.");
//                 return false;
//             }

//             if (chaveUnica == null || chaveUnica.isBlank()) {
//                 log.warn(
//                         "Não foi possível atualizar o MOSIA: chave única não informada. protocolo={}",
//                         protocolo);
//                 return false;
//             }

//             Integer idStatus = null;

//             if (statusValor != null && !statusValor.isBlank()) {
//                 try {
//                     idStatus = Integer.valueOf(statusValor.trim());
//                 } catch (NumberFormatException e) {
//                     idStatus = mapearCodigoPorNome(statusValor);
//                 }
//             }

//             if (idStatus == null) {
//                 log.warn(
//                         "Não foi possível atualizar o MOSIA: status não mapeado. protocolo={} status={}",
//                         protocolo,
//                         statusValor);
//                 return false;
//             }

//             String protocoloNormalizado = protocolo.trim();
//             String chaveNormalizada = chaveUnica.replaceAll("\\D", "");

//             String url = mosiaOcorrenciaUrl
//                     + "/"
//                     + protocoloNormalizado
//                     + "/status";

//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.APPLICATION_JSON);
//             headers.set("Authorization", mosiaToken);

//             MosiaStatusUpdateRequest request = new MosiaStatusUpdateRequest();

//             request.setIdNovoStatus(idStatus);
//             request.setTextoDesfecho(statusValor);
//             request.setProtocolo(protocoloNormalizado);
//             request.setChaveUnicaSolicitante(chaveNormalizada);
//             request.setManterAtribuicao(true);

//             String jsonEnvio = mapper
//                     .writerWithDefaultPrettyPrinter()
//                     .writeValueAsString(request);

//             log.info(
//                     "Enviando atualização para MOSIA: url={} payload={}",
//                     url,
//                     jsonEnvio);

//             HttpEntity<MosiaStatusUpdateRequest> entity = new HttpEntity<>(request, headers);

//             ResponseEntity<String> response = restTemplate.exchange(
//                     url,
//                     HttpMethod.PUT,
//                     entity,
//                     String.class);

//             log.info(
//                     "Retorno MOSIA -> {} : {}",
//                     response.getStatusCode(),
//                     response.getBody());

//             return response.getStatusCode().is2xxSuccessful();

//         } catch (Exception e) {
//             log.error(
//                     "Erro ao enviar atualização para MOSIA: {}",
//                     e.getMessage(),
//                     e);

//             return false;
//         }
//     }

//     /**
//      * Faz o mapeamento de nome do status para o ID correspondente no MOSIA.
//      * (método privado para uso interno)
//      */
//     private Integer mapearCodigoPorNome(String nomeStatus) {
//         if (nomeStatus == null)
//             return null;

//         switch (nomeStatus.trim().toUpperCase()) {

//             case "ENVIADA":
//                 return 4682;

//             case "SOLICITAÇÃO EM ANÁLISE":
//                 return 4681;

//             default:
//                 log.warn("Status MOSIA não mapeado: {}", nomeStatus);
//                 return null;
//         }
//     }

//     /**
//      * Método público que expõe o mapeamento nome->id para outros serviços.
//      * Útil para preencher statusId antes de persistir.
//      */
//     public Integer mapearCodigoPorNomePublic(String nomeStatus) {
//         return mapearCodigoPorNome(nomeStatus);
//     }
// }

package com.crm.integration.service;

import com.crm.integration.enums.MosiaStatusEnum;
import com.crm.integration.model.MosiaStatusUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final MosiaStatusConfiguracaoService statusConfiguracaoService;

    public MosiaIntegrationService(MosiaStatusConfiguracaoService statusConfiguracaoService) {
        this.statusConfiguracaoService = statusConfiguracaoService;
    }

    /**
     * Atualiza o status de uma ocorrência no MOSIA.
     *
     * O método utiliza primeiro o statusId recebido.
     * Caso o statusId esteja nulo, tenta obter o código pelo texto do status.
     *
     * @param protocolo   protocolo da ocorrência no MOSIA
     * @param statusId    código numérico do status no MOSIA
     * @param statusValor descrição textual do status
     * @param chaveUnica  chave única do solicitante
     * @return true quando o MOSIA retornar uma resposta HTTP 2xx
     */
    public boolean atualizarStatusNoMosia(
            String protocolo,
            Integer statusId,
            String statusValor,
            String chaveUnica) {

        try {
            if (protocolo == null || protocolo.isBlank()) {
                log.warn(
                        "Não foi possível atualizar o MOSIA: protocolo não informado.");
                return false;
            }

            if (chaveUnica == null || chaveUnica.isBlank()) {
                log.warn(
                        "Não foi possível atualizar o MOSIA: chave única não informada. protocolo={}",
                        protocolo);
                return false;
            }

            /*
             * Prioriza o STATUS_ID já salvo no banco.
             *
             * Caso esteja nulo, tenta:
             * 1. converter o conteúdo de statusValor em número;
             * 2. mapear o texto do status para o código correspondente.
             */
            Integer idStatus = statusId;

            if (idStatus == null
                    && statusValor != null
                    && !statusValor.isBlank()) {

                try {
                    idStatus = Integer.valueOf(statusValor.trim());
                } catch (NumberFormatException e) {
                    idStatus = mapearCodigoPorNome(statusValor);
                }
            }

            if (idStatus == null) {
                log.warn(
                        "Não foi possível atualizar o MOSIA: status sem ID ou mapeamento. "
                                + "protocolo={} status={} statusId={}",
                        protocolo,
                        statusValor,
                        statusId);
                return false;
            }

            String protocoloNormalizado = protocolo.trim();
            String chaveNormalizada = chaveUnica.replaceAll("\\D", "");

            if (chaveNormalizada.isBlank()) {
                log.warn(
                        "Não foi possível atualizar o MOSIA: chave única inválida após normalização. "
                                + "protocolo={}",
                        protocoloNormalizado);
                return false;
            }

            String url = mosiaOcorrenciaUrl
                    + "/"
                    + protocoloNormalizado
                    + "/status";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", mosiaToken);

            MosiaStatusUpdateRequest request = new MosiaStatusUpdateRequest();

            request.setIdNovoStatus(idStatus);
            request.setTextoDesfecho(statusValor);
            request.setProtocolo(protocoloNormalizado);
            request.setChaveUnicaSolicitante(chaveNormalizada);
            request.setManterAtribuicao(true);

            String jsonEnvio = mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request);

            log.info(
                    "Enviando atualização para MOSIA: url={} statusId={} payload={}",
                    url,
                    idStatus,
                    jsonEnvio);

            HttpEntity<MosiaStatusUpdateRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    String.class);

            log.info(
                    "Retorno MOSIA -> protocolo={} statusHttp={} body={}",
                    protocoloNormalizado,
                    response.getStatusCode(),
                    response.getBody());

            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error(
                    "Erro ao enviar atualização para MOSIA. protocolo={} statusId={} status={}. Erro: {}",
                    protocolo,
                    statusId,
                    statusValor,
                    e.getMessage(),
                    e);

            return false;
        }
    }

    /**
     * Faz o mapeamento do nome do status para o ID correspondente no MOSIA.
     *
     * Este mapeamento é utilizado somente quando o STATUS_ID não estiver
     * preenchido.
     */
    private Integer mapearCodigoPorNome(String nomeStatus) {

        if (nomeStatus == null || nomeStatus.isBlank()) {
            return null;
        }

        Integer codigoCatalogado = statusConfiguracaoService.buscarCodigoPorDescricao(nomeStatus);
        if (codigoCatalogado == null) {
            codigoCatalogado = MosiaStatusEnum.getCodigoPorNome(nomeStatus);
        }
        if (codigoCatalogado != null) {
            return codigoCatalogado;
        }

        switch (nomeStatus.trim().toUpperCase()) {

            case "ENVIADA":
                return 4682;

            case "SOLICITAÇÃO EM ANÁLISE":
                return 4681;

            case "FINALIZADO: CONSULTE AUTORIZAÇÕES":
                return 5192;

            default:
                log.warn(
                        "Status MOSIA não mapeado: {}",
                        nomeStatus);
                return null;
        }
    }

    /**
     * Expõe publicamente o mapeamento nome -> ID.
     *
     * Pode ser utilizado por controllers ou outros serviços antes de persistir
     * um status.
     */
    public Integer mapearCodigoPorNomePublic(String nomeStatus) {
        return mapearCodigoPorNome(nomeStatus);
    }
}
