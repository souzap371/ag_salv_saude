package com.crm.integration.controller;

import com.crm.integration.model.MensagemRequest;
import com.crm.integration.model.MensagemResponse;
import com.crm.integration.service.MensageriaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes/mensageria")
public class MensageriaController {

    private final MensageriaService mensageriaService;

    @Value("${mensageria.token}")
    private String mensageriaToken;

    public MensageriaController(MensageriaService mensageriaService) {
        this.mensageriaService = mensageriaService;
    }

    @PostMapping("/envio")
    public ResponseEntity<MensagemResponse> enviarMensagem(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MensagemRequest request) {

        if (!isValidToken(authorization)) {
            MensagemResponse response = new MensagemResponse(
                    false,
                    "Token de autorização inválido.",
                    401,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        var entity = mensageriaService.salvarMensagem(request, "9999", "1");

        MensagemResponse response = new MensagemResponse(
                true,
                "Mensagem registrada e enviada com sucesso.",
                200,
                System.currentTimeMillis(),
                entity);

        return ResponseEntity.ok(response);
    }

    private boolean isValidToken(String token) {
        return mensageriaToken.equals(token);
    }
}
