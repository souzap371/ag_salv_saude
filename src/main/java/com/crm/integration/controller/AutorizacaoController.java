package com.crm.integration.controller;

import com.crm.integration.mapper.AutorizacaoMapper;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.service.AutorizacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes/autorizacao")
public class AutorizacaoController {

        private static final Logger log = LoggerFactory.getLogger(
                        AutorizacaoController.class);

        private final AutorizacaoService autorizacaoService;
        private final AutorizacaoMapper autorizacaoMapper;

        public AutorizacaoController(
                        AutorizacaoService autorizacaoService,
                        AutorizacaoMapper autorizacaoMapper) {

                this.autorizacaoService = autorizacaoService;

                this.autorizacaoMapper = autorizacaoMapper;
        }

        /**
         * Hook "Autorização: Solicitado".
         *
         * Como o conteúdo veio da própria Mosia, ele será salvo
         * diretamente como sincronizado.
         */
        @PostMapping("/callback")
        public ResponseEntity<String> receiveCallback(
                        @RequestBody String rawBody) {

                log.info(
                                "[AUTORIZACAO CALLBACK] Callback recebido.");

                try {
                        AutorizacaoSolicitada entity = autorizacaoMapper
                                        .mapCallbackToEntity(rawBody);

                        if (entity == null) {
                                log.warn(
                                                "[AUTORIZACAO CALLBACK] "
                                                                + "Mapper retornou entidade nula.");

                                return ResponseEntity
                                                .badRequest()
                                                .body(
                                                                "Não foi possível interpretar o callback.");
                        }

                        /*
                         * O callback veio da própria Mosia.
                         */
                        entity.setSincronizadoMosia("S");
                        entity.setRawCallbackJson(rawBody);

                        autorizacaoService
                                        .saveAutorizacao(entity);

                        log.info(
                                        "[AUTORIZACAO CALLBACK] "
                                                        + "Callback processado. protocolo={}",
                                        entity.getProtocolo());

                        return ResponseEntity.ok(
                                        "Callback processado.");

                } catch (IllegalArgumentException e) {
                        log.warn(
                                        "[AUTORIZACAO CALLBACK] "
                                                        + "Callback inválido: {}",
                                        e.getMessage());

                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());

                } catch (Exception e) {
                        log.error(
                                        "[AUTORIZACAO CALLBACK] "
                                                        + "Erro ao processar callback.",
                                        e);

                        return ResponseEntity
                                        .status(
                                                        HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        "Erro ao processar callback.");
                }
        }

        /**
         * Registra uma alteração criada pelo sistema local.
         *
         * Esse endpoint não envia diretamente para a Mosia.
         * Ele cria uma pendência para o scheduler.
         */
        @PostMapping("/{protocolo}/status")
        public ResponseEntity<String> updateStatus(
                        @PathVariable String protocolo,
                        @RequestParam String status) {

                try {
                        AutorizacaoSolicitada novo = autorizacaoService
                                        .registrarNovoStatus(
                                                        protocolo,
                                                        status);

                        return ResponseEntity.ok(
                                        "Novo status registrado para sincronização. "
                                                        + "ID: "
                                                        + novo.getId());

                } catch (IllegalArgumentException e) {
                        return ResponseEntity
                                        .badRequest()
                                        .body(e.getMessage());

                } catch (Exception e) {
                        log.error(
                                        "Erro ao registrar novo status. "
                                                        + "protocolo={} status={}",
                                        protocolo,
                                        status,
                                        e);

                        return ResponseEntity
                                        .status(
                                                        HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        "Erro ao registrar novo status.");
                }
        }
}