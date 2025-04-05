package com.crm.integration.controller;

import com.crm.integration.model.WebhookCall;
import com.crm.integration.repository.WebhookCallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @Autowired
    private WebhookCallRepository webhookCallRepository;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> receiveWebhook(@RequestBody String payload) {
        // Armazena o payload recebido no banco de dados para processamento posterior.
        WebhookCall call = new WebhookCall(payload);
        webhookCallRepository.save(call);

        // Retorna status OK com uma mensagem de confirmação.
        return new ResponseEntity<>("{\"message\": \"Webhook recebido com sucesso.\"}", HttpStatus.OK);
    }
}
