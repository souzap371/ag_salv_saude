package com.crm.integration.controller;

import com.crm.integration.model.MensagemRequest;
import com.crm.integration.model.MensagemResponse;
import com.crm.integration.model.StatusUpdateRequest;
import com.crm.integration.model.OcorrenciaResponse;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.repository.OcorrenciaRepository;
import com.crm.integration.service.MensageriaService;
import com.crm.integration.service.OcorrenciaAtualizacaoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/clientes/mensageria")
public class MensageriaController {

    private final MensageriaService mensageriaService;
    private final OcorrenciaAtualizacaoService ocorrenciaAtualizacaoService;

    @Autowired
    private OcorrenciaRepository ocorrenciaRepository;

    @Value("${mensageria.token}")
    private String mensageriaToken;

    public MensageriaController(
            MensageriaService mensageriaService,
            OcorrenciaAtualizacaoService ocorrenciaAtualizacaoService) {
        this.mensageriaService = mensageriaService;
        this.ocorrenciaAtualizacaoService = ocorrenciaAtualizacaoService;
    }

    // ---------------------------------------------------------
    // ENVIO DE MENSAGEM
    // ---------------------------------------------------------
    @PostMapping("/envio")
    public ResponseEntity<MensagemResponse> enviarMensagem(
            @RequestHeader("Authorization") String authorization,
            @RequestBody MensagemRequest request) {

        if (!isValidToken(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponse(
                            false,
                            "Token de autorização inválido.",
                            401,
                            System.currentTimeMillis(),
                            null
                    ));
        }

        var entity = mensageriaService.salvarMensagem(request, "9999", "1");

        return ResponseEntity.ok(
                new MensagemResponse(
                        true,
                        "Mensagem registrada e enviada com sucesso.",
                        200,
                        System.currentTimeMillis(),
                        entity
                )
        );
    }

    // ---------------------------------------------------------
    // ATUALIZAÇÃO DE STATUS DA OCORRÊNCIA
    // ---------------------------------------------------------
    @PostMapping("/atualizar-status/{protocolo}")
    public ResponseEntity<OcorrenciaResponse> atualizarStatusPorMensageria(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String protocolo,
            @RequestBody StatusUpdateRequest request) {

        if (!isValidToken(authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new OcorrenciaResponse(
                            false,
                            "Token de autorização inválido.",
                            401,
                            System.currentTimeMillis(),
                            null
                    ));
        }

        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            return ResponseEntity.badRequest().body(new OcorrenciaResponse(
                    false, "O status é obrigatório.", 400, System.currentTimeMillis(), null));
        }
        try {
            var resultado = ocorrenciaAtualizacaoService.atualizarStatus(
                    protocolo, null, request.getStatus(), null);
            if (request.getRascunho() != null) {
                resultado.ocorrencia().setRascunho(request.getRascunho());
                ocorrenciaRepository.save(resultado.ocorrencia());
            }
            int codigo = resultado.sincronizadoMosia() ? 200 : 207;
            return ResponseEntity.status(codigo).body(new OcorrenciaResponse(
                    true,
                    resultado.sincronizadoMosia()
                            ? "Status atualizado, sincronizado e mensagem registrada."
                            : "Status atualizado e mensagem registrada; sincronização com o MOSIA pendente.",
                    codigo,
                    System.currentTimeMillis(),
                    resultado.ocorrencia()));
        } catch (OcorrenciaAtualizacaoService.OcorrenciaNaoEncontradaException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OcorrenciaResponse(
                    false, e.getMessage(), 404, System.currentTimeMillis(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new OcorrenciaResponse(
                    false, e.getMessage(), 400, System.currentTimeMillis(), null));
        }
    }

    // ---------------------------------------------------------
    // VALIDAÇÃO DO TOKEN
    // ---------------------------------------------------------
    private boolean isValidToken(String token) {
        return mensageriaToken.equals(token);
    }
}
