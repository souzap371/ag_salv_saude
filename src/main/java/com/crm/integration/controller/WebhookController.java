package com.crm.integration.controller;

import com.crm.integration.model.WebhookCall;
import com.crm.integration.repository.WebhookCallRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Value("${mosia.token}")
    private String mosiaToken;

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private WebhookCallRepository webhookCallRepository;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> receiveWebhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody String payload) {

        log.info("🔔 Webhook recebido!");
        log.info("Authorization Header: {}", authorization);
        log.info("Payload: {}", payload);

        if (authorization == null || !authorization.equals(mosiaToken)) {
            log.warn("❌ Token inválido recebido: {}", authorization);
            return new ResponseEntity<>("{\"message\": \"Token inválido.\"}", HttpStatus.UNAUTHORIZED);
        }
        // Armazena o payload recebido no banco de dados para processamento posterior.
        WebhookCall call = new WebhookCall(payload);
        webhookCallRepository.save(call);

        // Retorna status OK com uma mensagem de confirmação.
        log.info("✅ Webhook processado e salvo com sucesso.");
        return new ResponseEntity<>("{\"message\": \"Webhook recebido com sucesso.\"}", HttpStatus.OK);
    }
}
