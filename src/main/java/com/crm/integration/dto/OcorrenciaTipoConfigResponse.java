package com.crm.integration.dto;

public record OcorrenciaTipoConfigResponse(
        Long id,
        String tipoNegocio,
        String idFuncionalidade,
        Integer idTipoOcorrencia,
        Integer idAssunto,
        boolean ativo) {
}
