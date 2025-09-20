package com.crm.integration.controller;

import com.crm.integration.dto.CallbackSolicitacaoDTO;
import com.crm.integration.dto.FormCampoDTO;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.service.AutorizacaoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/clientes/autorizacao")
public class AutorizacaoController {

    private final AutorizacaoService autorizacaoService;
    private final ObjectMapper mapper;

    public AutorizacaoController(AutorizacaoService autorizacaoService, ObjectMapper mapper) {
        this.autorizacaoService = autorizacaoService;
        this.mapper = mapper;
    }

    /**
     * Endpoint que a Mobile Saúde chamará via callback quando houver envio de
     * formulário.
     */
    // @PostMapping("/callback")
    // public ResponseEntity<String> receiveCallback(@RequestBody
    // CallbackSolicitacaoDTO callbackDto,
    // HttpServletRequest req,
    // @RequestHeader(value = "X-MobileSaude-Token", required = false) String token)
    // {
    // // 1) Segurança: validar token/IP conforme configuração (implemente conforme
    // // contrato)
    // String ip = req.getRemoteAddr();
    // // TODO: validar IP allowlist ou header token

    // try {
    // autorizacaoService.processCallback(callbackDto);
    // return ResponseEntity.ok("Callback processado");
    // } catch (Exception e) {
    // e.printStackTrace();
    // // retorna 500 para que o emissor (MobileSaude) possa tentar novamente se
    // // necessário
    // return ResponseEntity.status(500).body("Erro ao processar callback");
    // }
    // }
    @PostMapping("/callback")
    public ResponseEntity<String> receiveCallback(@RequestBody String rawBody, HttpServletRequest req) {
        System.out.println("Callback cru recebido: " + rawBody);
        try {
            CallbackSolicitacaoDTO dto = mapRawToCallbackDTO(rawBody);
            System.out.println("DTO convertido: " + mapper.writeValueAsString(dto));
            autorizacaoService.processCallback(dto);
            return ResponseEntity.ok("Callback processado");
        } catch (IllegalArgumentException e) {
            // protocolo inválido — log e retorna 200 para não causar retry desnecessário
            System.err.println("Callback inválido: " + e.getMessage());
            return ResponseEntity.ok("Callback inválido (ignorando)");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao processar callback");
        }
    }

    private CallbackSolicitacaoDTO mapRawToCallbackDTO(String rawBody) throws Exception {
        JsonNode root = mapper.readTree(rawBody);
        JsonNode ocorrencia = root.path("ocorrencia");

        String protocolo = ocorrencia.path("protocolo").asText(null);
        CallbackSolicitacaoDTO dto = new CallbackSolicitacaoDTO();
        dto.setProtocolo(protocolo);

        // outros campos simples (se existirem em outros lugares do payload)
        // dto.setNome(...); // somente se fizer sentido mapear

        // montar lista de FormCampoDTO a partir de
        // ocorrencia.solicitacao.formularios[*].itens[*]
        List<FormCampoDTO> campos = new ArrayList<>();
        JsonNode solicitacao = ocorrencia.path("solicitacao");
        JsonNode formularios = solicitacao.path("formularios");
        if (formularios.isArray()) {
            for (JsonNode formulario : formularios) {
                JsonNode itens = formulario.path("itens");
                if (itens.isArray()) {
                    for (JsonNode item : itens) {
                        // cada 'item' pode ter vários campos (nome-do-contato, celular-do-contato,
                        // imagem-da-guia-solicitacao, ...)
                        Iterator<Map.Entry<String, JsonNode>> fields = item.fields();
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> f = fields.next();
                            String fieldName = f.getKey();
                            JsonNode valNode = f.getValue();

                            if (valNode.isArray()) {
                                // array: tipicamente imagens ou múltiplos valores
                                for (JsonNode el : valNode) {
                                    // se é objeto e tem "url", pegue a url
                                    String value = el.has("url") ? el.path("url").asText(null) : el.asText(null);
                                    if (value != null) {
                                        FormCampoDTO campo = new FormCampoDTO();
                                        campo.setLabel(fieldName);
                                        campo.setValue(value);
                                        campos.add(campo);
                                    }
                                }
                            } else {
                                String value = valNode.isNull() ? null : valNode.asText(null);
                                if (value != null) {
                                    FormCampoDTO campo = new FormCampoDTO();
                                    campo.setLabel(fieldName);
                                    campo.setValue(value);
                                    campos.add(campo);
                                }
                            }
                        }
                    }
                }
            }
        }

        dto.setCampos(campos);
        return dto;
    }

    @GetMapping("/{protocolo}")
    public ResponseEntity<AutorizacaoSolicitada> getByProtocolo(@PathVariable String protocolo) {
        return autorizacaoService.findByProtocolo(protocolo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{protocolo}/status")
    public ResponseEntity<String> updateStatus(@PathVariable String protocolo, @RequestParam String status) {
        autorizacaoService.updateStatusLocal(protocolo, status);
        return ResponseEntity.ok("Status atualizado localmente");
    }
}
