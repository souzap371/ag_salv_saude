// package com.crm.integration.service;

// import com.crm.integration.model.AgMensApp;
// import com.crm.integration.model.MensagemRequest;
// import com.crm.integration.repository.AgMensAppRepository;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.*;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import java.time.LocalDateTime;
// import java.time.ZoneId;

// @Service
// public class MensageriaService {

//     // private final AgMensAppRepository repository;
//     // private final RestTemplate restTemplate = new RestTemplate();

//     private final AgMensAppRepository repository;
//     private final RestTemplate restTemplate;

//     @Value("${mosia.api.url}")
//     private String mosiaApiUrl;

//     @Value("${mensageria.token}")
//     private String mosiaApiToken;

//     // public MensageriaService(AgMensAppRepository repository) {
//     // this.repository = repository;
//     // }

//     public MensageriaService(
//             AgMensAppRepository repository,
//             RestTemplate restTemplate) {
//         this.repository = repository;
//         this.restTemplate = restTemplate;
//     }

//     // Salva no banco e envia imediatamente
//     // public AgMensApp salvarMensagem(MensagemRequest request, String
//     // idFuncionalidade, String idRegistro) {
//     // AgMensApp entity = new AgMensApp();
//     // entity.setChaveUnica(request.getChaveUnica());
//     // entity.setMensagem(request.getMsg());
//     // ZoneId zone = ZoneId.of("America/Sao_Paulo");
//     // entity.setDtInclusao(LocalDateTime.now(zone));
//     // entity.setIdFuncionalidade(idFuncionalidade);
//     // entity.setIdRegistro(idRegistro);
//     // repository.save(entity);

//     // // Envia para Mosia
//     // enviarParaMosia(request);

//     // return entity;
//     // }

//     public AgMensApp salvarMensagem(
//             MensagemRequest request,
//             String idFuncionalidade,
//             String idRegistro) {
//         ZoneId zone = ZoneId.of("America/Sao_Paulo");

//         AgMensApp entity = new AgMensApp();
//         entity.setChaveUnica(request.getChaveUnica());
//         entity.setMensagem(request.getMsg());
//         entity.setDtInclusao(LocalDateTime.now(zone));
//         entity.setIdFuncionalidade(idFuncionalidade);
//         entity.setIdRegistro(idRegistro);
//         entity.setProc("N");

//         entity = repository.save(entity);

//         boolean enviado = enviarParaMosia(request);

//         if (enviado) {
//             entity.setDtEnvio(LocalDateTime.now(zone));
//             entity.setProc("S");
//             entity = repository.save(entity);
//         }

//         return entity;
//     }

//     public boolean enviarParaMosia(MensagemRequest request) {
//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         headers.set("Authorization", mosiaApiToken);

//         HttpEntity<MensagemRequest> entity = new HttpEntity<>(request, headers);

//         try {
//             System.out.println("[SERVICE] Enviando para Mosia - Chave: " + request.getChaveUnica() +
//                     " | Msg: " + request.getMsg());

//             ResponseEntity<String> response = restTemplate.postForEntity(mosiaApiUrl, entity, String.class);

//             System.out.println("[SERVICE] Resposta Mosia: HTTP " + response.getStatusCode() +
//                     " | Body: " + response.getBody());

//             if (!response.getStatusCode().is2xxSuccessful()) {
//                 System.out.println("[SERVICE] Falha ao enviar para Mosia: " + response.getStatusCode());
//                 return false;
//             } else {
//                 System.out.println("[SERVICE] Mensagem enviada com sucesso.");
//                 return true;
//             }
//         } catch (Exception e) {
//             System.out.println("[SERVICE] Erro ao enviar para Mosia: " + e.getMessage());
//             return false;
//         }
//     }

// }

package com.crm.integration.service;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class MensageriaService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final AgMensAppRepository repository;
    private final RestTemplate restTemplate;

    @Value("${mosia.api.url}")
    private String mosiaApiUrl;

    @Value("${mensageria.token}")
    private String mosiaApiToken;

    public MensageriaService(
            AgMensAppRepository repository,
            RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    public AgMensApp salvarMensagem(
            MensagemRequest request,
            String idFuncionalidade,
            String idRegistro) {

        validarRequest(request);

        AgMensApp entity = new AgMensApp();
        entity.setChaveUnica(request.getChaveUnica());
        entity.setMensagem(request.getMsg());
        entity.setDtInclusao(LocalDateTime.now(ZONE_ID));
        entity.setIdFuncionalidade(idFuncionalidade);
        entity.setIdRegistro(idRegistro);
        entity.setProc("N");

        entity = repository.save(entity);

        boolean enviado = enviarParaMosia(request);

        if (enviado) {
            entity.setDtEnvio(LocalDateTime.now(ZONE_ID));
            entity.setProc("S");
            entity = repository.save(entity);
        }

        return entity;
    }

    /** Registra a mensagem para o scheduler, sem realizar envio dentro da transação atual. */
    public AgMensApp enfileirarMensagem(
            MensagemRequest request,
            String idFuncionalidade,
            String idRegistro) {
        return enfileirarMensagem(request, idFuncionalidade, idRegistro, null);
    }

    public AgMensApp enfileirarMensagem(
            MensagemRequest request,
            String idFuncionalidade,
            String idRegistro,
            Long numeroGuia) {
        validarRequest(request);
        AgMensApp entity = new AgMensApp();
        entity.setChaveUnica(request.getChaveUnica().trim());
        entity.setMensagem(request.getMsg().trim());
        entity.setDtInclusao(LocalDateTime.now(ZONE_ID));
        entity.setIdFuncionalidade(idFuncionalidade);
        entity.setIdRegistro(idRegistro);
        entity.setNumeroGuia(numeroGuia);
        entity.setProc("N");
        return repository.save(entity);
    }

    public boolean enviarParaMosia(MensagemRequest request) {

        if (!requestValido(request)) {
            System.out.println(
                    "[SERVICE] Envio ignorado: chave ou mensagem inválida.");
            return false;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", mosiaApiToken);

        HttpEntity<MensagemRequest> entity = new HttpEntity<>(request, headers);

        try {
            System.out.println(
                    "[SERVICE] Enviando para Mosia - Chave: " +
                            request.getChaveUnica());

            ResponseEntity<String> response = restTemplate.postForEntity(
                    mosiaApiUrl,
                    entity,
                    String.class);

            System.out.println(
                    "[SERVICE] Resposta Mosia: HTTP " +
                            response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println(
                        "[SERVICE] Mensagem enviada com sucesso.");
                return true;
            }

            System.out.println(
                    "[SERVICE] Falha ao enviar para Mosia: " +
                            response.getStatusCode());

            return false;

        } catch (HttpStatusCodeException e) {
            System.out.println(
                    "[SERVICE] Erro HTTP ao enviar para Mosia: " +
                            e.getStatusCode() +
                            " | Resposta: " +
                            e.getResponseBodyAsString());

            return false;

        } catch (Exception e) {
            System.out.println(
                    "[SERVICE] Erro ao enviar para Mosia: " +
                            e.getMessage());

            return false;
        }
    }

    private void validarRequest(MensagemRequest request) {
        if (!requestValido(request)) {
            throw new IllegalArgumentException(
                    "Chave única e mensagem são obrigatórias.");
        }
    }

    private boolean requestValido(MensagemRequest request) {
        return request != null
                && request.getChaveUnica() != null
                && !request.getChaveUnica().isBlank()
                && request.getMsg() != null
                && !request.getMsg().isBlank();
    }
}
