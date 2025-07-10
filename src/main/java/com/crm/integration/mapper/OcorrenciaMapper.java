package com.crm.integration.mapper;

import com.crm.integration.dto.*;
import com.crm.integration.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OcorrenciaMapper {

    public static Ocorrencia fromMosia(OcorrenciaMosia mosia) {
        Ocorrencia ocorrencia = new Ocorrencia();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        ocorrencia.setProtocolo(mosia.getProtocolo());
        ocorrencia.setDataRegistro(LocalDateTime.parse(mosia.getDataRegistro(), fmt));
        ocorrencia.setDataAtualizacao(LocalDateTime.parse(mosia.getDataAtualizacao(), fmt));
        ocorrencia.setIdTipoOcorrencia(mosia.getTipoOcorrencia().getId());
        ocorrencia.setIdAssunto(mosia.getAssunto().getId());
        ocorrencia.setNumeroContrato(mosia.getSolicitante().getNumeroContrato());

        // Solicitante
        Solicitante solicitante = new Solicitante();
        solicitante.setChaveUnica(mosia.getSolicitante().getChaveUnica());
        ocorrencia.setSolicitante(solicitante);

        // Beneficiário
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setChaveUnica(mosia.getBeneficiario().getChaveUnica());
        ocorrencia.setBeneficiario(beneficiario);

        return ocorrencia;
    }
}
