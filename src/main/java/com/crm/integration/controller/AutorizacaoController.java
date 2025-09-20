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

    @GetMapping("/{protocolo}")
    public ResponseEntity<AutorizacaoSolicitada> getByProtocolo(@PathVariable String protocolo) {
        return autorizacaoService.findByProtocolo(protocolo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{protocolo}/status")
    public ResponseEntity<String> updateStatus(@PathVariable String protocolo,
            @RequestParam String status) {
        autorizacaoService.updateStatusLocal(protocolo, status);
        return ResponseEntity.ok("Status atualizado localmente");
    }
}
