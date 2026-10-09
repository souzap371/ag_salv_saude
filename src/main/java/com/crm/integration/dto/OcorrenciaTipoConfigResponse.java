package com.crm.integration.dto;

import com.crm.integration.enums.TipoNegocioOcorrencia;

public record OcorrenciaTipoConfigResponse(
        Long id,
        TipoNegocioOcorrencia tipoNegocio,
        Integer idFuncionalidade,
        Integer idTipoOcorrencia,
        Integer idAssunto,
        boolean ativo) {
}
