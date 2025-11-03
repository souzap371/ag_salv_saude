package com.crm.integration.controller;

import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.service.AutorizacaoService;
import com.crm.integration.mapper.AutorizacaoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/clientes/autorizacao")
public class AutorizacaoController {

    private final AutorizacaoService autorizacaoService;
    private final AutorizacaoMapper autorizacaoMapper;

    public AutorizacaoController(AutorizacaoService autorizacaoService,
            AutorizacaoMapper autorizacaoMapper) {
        this.autorizacaoService = autorizacaoService;
        this.autorizacaoMapper = autorizacaoMapper;
    }

    @PostMapping("/callback")
    public ResponseEntity<String> receiveCallback(@RequestBody String rawBody,
            HttpServletRequest req) {
        System.out.println("Callback cru recebido: " + rawBody);
        try {
            AutorizacaoSolicitada entity = autorizacaoMapper.mapCallbackToEntity(rawBody);
            autorizacaoService.saveAutorizacao(entity);
            return ResponseEntity.ok("Callback processado");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao processar callback");
        }
    }
    @PostMapping("/{protocolo}/status")
public ResponseEntity<String> updateStatus(@PathVariable String protocolo,
        @RequestParam String status) {
    AutorizacaoSolicitada novo = autorizacaoService.registrarNovoStatus(protocolo, status);
    return ResponseEntity.ok("Novo status registrado e Mosia notificado. ID: " + novo.getId());
}

@PostMapping("/callback/status")
public ResponseEntity<String> receiveStatusCallback(@RequestBody String rawBody) {
    System.out.println("Callback de STATUS recebido: " + rawBody);
    try {
        // Extrair protocolo e status do JSON recebido
        String protocolo = extrairCampo(rawBody, "protocolo");
        String status = extrairCampo(rawBody, "status");

        if (protocolo == null || status == null) {
            return ResponseEntity.badRequest().body("Campos 'protocolo' e 'status' são obrigatórios");
        }

        // Atualiza o status e notifica Mosia
        autorizacaoService.registrarNovoStatus(protocolo, status);

        return ResponseEntity.ok("Status atualizado para " + status + " no protocolo " + protocolo);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Erro ao processar callback de status");
    }
}

/**
 * Método auxiliar simples para extrair campos de JSON sem precisar criar DTO.
 */
private String extrairCampo(String json, String campo) {
    try {
        com.fasterxml.jackson.databind.JsonNode node =
                new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
        return node.has(campo) ? node.get(campo).asText() : null;
    } catch (Exception e) {
        return null;
    }
}


}
