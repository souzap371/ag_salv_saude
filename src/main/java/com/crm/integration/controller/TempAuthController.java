package com.crm.integration.controller;


import com.crm.integration.model.DataToken;
import com.crm.integration.model.ErrorResponse;
import com.crm.integration.model.TempAuthResponse;
import com.crm.integration.util.TokenUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@RestController
@ConditionalOnProperty(name = "features.legacy-endpoints.enabled", havingValue = "true")
@RequestMapping("/clientes/auth")
public class TempAuthController {

    // Valor fixo para simulação. Em produção, este valor pode vir de configuração.
    private static final String VALID_AUTHORIZATION = "OperadoraToken123";

    @GetMapping(value = "/temp-authorization", produces = "application/json")
    public ResponseEntity<?> generateTempToken(@RequestHeader(value = "Authorization", required = true) String authHeader,
                                               @RequestHeader(value = "Content-Type", required = true) String contentType) {

        // Valida se o header de autorização está correto.
        if (!VALID_AUTHORIZATION.equals(authHeader)) {
            ErrorResponse error = new ErrorResponse(false, "Invalid token.");
            return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
        }

        // Gera token e calcula data de expiração (12 horas).
        String token = TokenUtil.generateToken();
        long expireAt = TokenUtil.getExpirationTimestamp();

        DataToken dataToken = new DataToken(token, expireAt);
        TempAuthResponse response = new TempAuthResponse(true, dataToken,
                "Token gerado com sucesso!", HttpStatus.OK.value(), System.currentTimeMillis());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
