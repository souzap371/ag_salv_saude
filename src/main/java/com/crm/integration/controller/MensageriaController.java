package com.crm.integration.controller;

import com.crm.integration.model.MensagemRequest;
import com.crm.integration.model.WhatsappMensagemRequest;
import com.crm.integration.service.MensageriaService;
import com.crm.integration.model.WhatsappMensagemComMidiaRequest;
import com.crm.integration.model.MensagemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/clientes/mensageria")
// public class MensageriaController {

//     // Método de envio de mensagem padrão
//     @PostMapping("/mensagem")
//     public ResponseEntity<MensagemResponse> enviarMensagem(
//             @RequestHeader("Authorization") String authorization,
//             @RequestBody MensagemRequest request) {

//         if (!isValidToken(authorization)) {
//             MensagemResponse response = new MensagemResponse(
//                     false,
//                     "Token de autorização inválido.",
//                     401,
//                     System.currentTimeMillis(),
//                     null);
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//         }

//         // Aqui você implementa a lógica para envio de mensagem
//         // Por exemplo, chamar um serviço de mensageria, salvar logs, etc.
//         MensagemResponse response = new MensagemResponse(
//                 true,
//                 "Mensagem enviada com sucesso.",
//                 200,
//                 System.currentTimeMillis(),
//                 null);
//         return ResponseEntity.ok(response);
//     }

//     // Método de envio de mensagem via WhatsApp
//     @PostMapping("/whatsapp")
//     public ResponseEntity<MensagemResponse> enviarMensagemWhatsapp(
//             @RequestHeader("Authorization") String authorization,
//             @RequestBody WhatsappMensagemRequest request) {

//         if (!isValidToken(authorization)) {
//             MensagemResponse response = new MensagemResponse(
//                     false,
//                     "Token de autorização inválido.",
//                     401,
//                     System.currentTimeMillis(),
//                     null);
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//         }

//         // Implementar a lógica para envio de mensagem via WhatsApp
//         MensagemResponse response = new MensagemResponse(
//                 true,
//                 "Mensagem WhatsApp enviada com sucesso.",
//                 200,
//                 System.currentTimeMillis(),
//                 null);
//         return ResponseEntity.ok(response);
//     }

//     // Método de envio de mensagem com mídia via WhatsApp
//     @PostMapping("/whatsapp/midia")
//     public ResponseEntity<MensagemResponse> enviarMensagemWhatsappComMidia(
//             @RequestHeader("Authorization") String authorization,
//             @RequestBody WhatsappMensagemComMidiaRequest request) {

//         if (!isValidToken(authorization)) {
//             MensagemResponse response = new MensagemResponse(
//                     false,
//                     "Token de autorização inválido.",
//                     401,
//                     System.currentTimeMillis(),
//                     null);
//             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
//         }

//         // Implementar a lógica para envio de mensagem com mídia via WhatsApp
//         MensagemResponse response = new MensagemResponse(
//                 true,
//                 "Mensagem WhatsApp com mídia enviada com sucesso.",
//                 200,
//                 System.currentTimeMillis(),
//                 null);
//         return ResponseEntity.ok(response);
//     }

//     // Método para validação do token (placeholder)
//     private boolean isValidToken(String token) {
//         // Implemente aqui a validação real do token conforme a sua documentação
//         return true; // Placeholder
//     }
// }
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/clientes/mensageria")
public class MensageriaController {

    private final MensageriaService mensageriaService;

    // injeta o token configurado no application.properties
    @Value("${mensageria.token}")
    private String mensageriaToken;

    public MensageriaController(MensageriaService mensageriaService) {
        this.mensageriaService = mensageriaService;
    }

    @PostMapping("/mensagem")
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

        var entity = mensageriaService.salvarMensagem(request, 9999L, 1L);

        MensagemResponse response = new MensagemResponse(
                true,
                "Mensagem registrada com sucesso.",
                200,
                System.currentTimeMillis(),
                entity);

        return ResponseEntity.ok(response);
    }

    private boolean isValidToken(String token) {
        // compara exatamente com o valor configurado
        return mensageriaToken.equals(token);
    }
}
