package com.crm.integration.dto;

import com.crm.integration.enums.TipoNegocioOcorrencia;

public record OcorrenciaTipoConfigRequest(
        TipoNegocioOcorrencia tipoNegocio,
        String idFuncionalidade,
        Integer idTipoOcorrencia,
        Integer idAssunto,
        Boolean ativo) {
}
