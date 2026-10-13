package com.crm.integration.controller;


import com.crm.integration.model.SessaoTokenData;
import com.crm.integration.model.SessaoTokenResponse;
import com.crm.integration.util.TokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController
@ConditionalOnProperty(name = "features.legacy-endpoints.enabled", havingValue = "true")
@RequestMapping("/clientes")
public class SessaoController {

    /**
     * Endpoint para obter token de sessão para uma chave única conhecida.
     * Exemplo de URL: GET /clientes/sessao/{chave}
     */
    @GetMapping("/sessao/{chave}")
    public ResponseEntity<SessaoTokenResponse> obterTokenSessao(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("chave") String chave) {

        // Validação do token de autorização
        if (!isValidToken(authorization)) {
            SessaoTokenResponse response = new SessaoTokenResponse(
                    false,
                    null,
                    "Token de autorização inválido.",
                    401,
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        // Validação da chave única
        if (chave == null || chave.trim().isEmpty()) {
            SessaoTokenResponse response = new SessaoTokenResponse(
                    false,
                    null,
                    "Chave única inválida.",
                    400,
                    System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Gerar token de sessão e definir data de expiração (por exemplo, 1 hora a partir de agora)
        String token = TokenUtil.generateToken();
        long expireAt = System.currentTimeMillis() + (3600 * 1000); // 1 hora em milissegundos

        SessaoTokenData data = new SessaoTokenData(token, expireAt);
        SessaoTokenResponse response = new SessaoTokenResponse(
                true,
                data,
                "Token de sessão gerado com sucesso.",
                200,
                System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // Placeholder para a validação do token de autorização
    private boolean isValidToken(String token) {
        // Implemente a lógica real de validação do token conforme as especificações da documentação.
        return true;
    }
}
