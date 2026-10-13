package com.crm.integration.dto;

import java.time.OffsetDateTime;

public record MosiaStatusCatalogoResponse(
        Long id,
        Integer idStatusMosia,
        Integer idTipoOcorrenciaMosia,
        String descricao,
        boolean ativo,
        String origem,
        OffsetDateTime primeiraIdentificacao,
        OffsetDateTime ultimaIdentificacao) {
}
