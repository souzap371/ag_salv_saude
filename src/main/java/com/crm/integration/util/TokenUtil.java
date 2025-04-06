package com.crm.integration.util;


import java.util.UUID;

public class TokenUtil {
    
    public static String generateToken() {
        // Aqui implementar a lógica de geração de JWT se necessário.
        // Para simplicidade, utilizando um UUID como token simulado.
        return UUID.randomUUID().toString();
    }
    
    public static long getExpirationTimestamp() {
        // Retorna o timestamp para 12 horas após a geração.
        return System.currentTimeMillis() + (12 * 60 * 60 * 1000);
    }
}
