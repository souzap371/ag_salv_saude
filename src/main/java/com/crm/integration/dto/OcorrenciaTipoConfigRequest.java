package com.crm.integration.dto;

public record OcorrenciaTipoConfigRequest(
        String tipoNegocio,
        String idFuncionalidade,
        Integer idTipoOcorrencia,
        Integer idAssunto,
        Boolean ativo) {
}
