package com.crm.integration.controller;

import com.crm.integration.dto.OcorrenciaTipoConfigRequest;
import com.crm.integration.dto.OcorrenciaTipoConfigResponse;
import com.crm.integration.service.OcorrenciaTipoConfiguracaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@RestController
@RequestMapping("/admin/ocorrencia-tipos")
public class OcorrenciaTipoConfiguracaoController {

    private final OcorrenciaTipoConfiguracaoService service;
    private final String adminToken;

    public OcorrenciaTipoConfiguracaoController(
            OcorrenciaTipoConfiguracaoService service,
            @Value("${painel.admin.token:}") String adminToken) {
        this.service = service;
        this.adminToken = adminToken;
    }

    @GetMapping
    public List<OcorrenciaTipoConfigResponse> listar(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {
        autenticar(token);
        return service.listar();
    }

    @PostMapping
    public ResponseEntity<OcorrenciaTipoConfigResponse> criar(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @RequestBody OcorrenciaTipoConfigRequest request) {
        autenticar(token);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @PutMapping("/{id}")
    public OcorrenciaTipoConfigResponse atualizar(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable Long id,
            @RequestBody OcorrenciaTipoConfigRequest request) {
        autenticar(token);
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @RequestHeader(value = "X-Admin-Token", required = false) String token,
            @PathVariable Long id) {
        autenticar(token);
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private void autenticar(String token) {
        if (adminToken == null || adminToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "O acesso administrativo ainda não foi configurado.");
        }
        byte[] esperado = adminToken.getBytes(StandardCharsets.UTF_8);
        byte[] recebido = token == null ? new byte[0] : token.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(esperado, recebido)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token administrativo inválido.");
        }
    }
}
