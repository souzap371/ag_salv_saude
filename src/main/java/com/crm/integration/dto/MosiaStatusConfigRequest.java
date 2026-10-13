package com.crm.integration.dto;

public record MosiaStatusConfigRequest(
        Integer idStatusMosia,
        Integer idTipoOcorrenciaMosia,
        String descricao,
        Boolean ativo) {
}
