package com.crm.integration.controller;

import com.crm.integration.model.MensagemRequest;
import com.crm.integration.model.MensagemResponse;
import com.crm.integration.model.StatusUpdateRequest;
import com.crm.integration.model.OcorrenciaResponse;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.repository.OcorrenciaRepository;
import com.crm.integration.service.MensageriaService;

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

    @Autowired
    private OcorrenciaRepository ocorrenciaRepository;

    @Value("${mensageria.token}")
    private String mensageriaToken;

    public MensageriaController(MensageriaService mensageriaService) {
        this.mensageriaService = mensageriaService;
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

        // 🔥 CORREÇÃO: findByProtocolo retorna List<Ocorrencia>
        List<Ocorrencia> lista = ocorrenciaRepository.findByProtocolo(protocolo);

        if (lista.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new OcorrenciaResponse(
                            false,
                            "Ocorrência não encontrada.",
                            404,
                            System.currentTimeMillis(),
                            null
                    ));
        }

        Ocorrencia ocorrencia = lista.get(0);

        // Atualiza rascunho se enviado
        if (request.getRascunho() != null) {
            ocorrencia.setRascunho(request.getRascunho());
        }

        // Atualiza status se enviado
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            ocorrencia.setStatus(request.getStatus());
        }

        // Atualiza data
        ocorrencia.setDataAtualizacao(LocalDateTime.now());

        Ocorrencia ocorrenciaAtualizada = ocorrenciaRepository.save(ocorrencia);

        return ResponseEntity.ok(
                new OcorrenciaResponse(
                        true,
                        "Status atualizado via mensageria com sucesso.",
                        200,
                        System.currentTimeMillis(),
                        ocorrenciaAtualizada
                )
        );
    }

    // ---------------------------------------------------------
    // VALIDAÇÃO DO TOKEN
    // ---------------------------------------------------------
    private boolean isValidToken(String token) {
        return mensageriaToken.equals(token);
    }
}
