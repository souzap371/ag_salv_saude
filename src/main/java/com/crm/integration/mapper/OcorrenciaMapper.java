package com.crm.integration.mapper;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.model.Beneficiario;
import com.crm.integration.model.Formulario;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaStatusHistorico;
import com.crm.integration.model.Solicitante;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public final class OcorrenciaMapper {
    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private OcorrenciaMapper() {}

    public static Ocorrencia fromMosia(MosiaRequestDTO.OcorrenciaMosia dto) {
        if (dto == null) return null;

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setProtocolo(dto.getProtocolo());
        ocorrencia.setDataRegistro(converterData(dto.getDataRegistro()));
        ocorrencia.setDataAtualizacao(converterData(dto.getDataAtualizacao()));

        if (dto.getTipoOcorrencia() != null && dto.getTipoOcorrencia().getId() != null) {
            ocorrencia.setIdTipoOcorrencia(dto.getTipoOcorrencia().getId());
        }
        if (dto.getAssunto() != null) ocorrencia.setIdAssunto(dto.getAssunto().getId());
        if (dto.getSolicitante() != null) ocorrencia.setSolicitante(solicitante(dto.getSolicitante()));
        if (dto.getBeneficiario() != null) {
            ocorrencia.setBeneficiario(beneficiario(dto.getBeneficiario()));
            ocorrencia.setNumeroContrato(dto.getBeneficiario().getNumeroContrato());
        } else if (dto.getSolicitante() != null) {
            ocorrencia.setNumeroContrato(dto.getSolicitante().getNumeroContrato());
        }

        mapearFormularios(dto, ocorrencia);
        mapearStatus(dto, ocorrencia);
        return ocorrencia;
    }

    private static void mapearFormularios(MosiaRequestDTO.OcorrenciaMosia dto, Ocorrencia ocorrencia) {
        var formularios = new ArrayList<Formulario>();
        if (dto.getSolicitacao() != null && dto.getSolicitacao().getFormularios() != null) {
            dto.getSolicitacao().getFormularios().forEach(payload -> {
                Formulario formulario = new Formulario();
                formulario.setIdForm(payload.getIdForm());
                formulario.setNome(payload.getNome());
                formulario.setFormDataJson(payload.getItens());
                formularios.add(formulario);
            });
        }
        ocorrencia.setFormularios(formularios);
    }

    private static void mapearStatus(MosiaRequestDTO.OcorrenciaMosia dto, Ocorrencia ocorrencia) {
        if (dto.getAtendimento() == null || dto.getAtendimento().getStatus() == null) return;
        var status = dto.getAtendimento().getStatus();
        ocorrencia.setStatus(status.getDescricao());
        if (status.getDescricao() != null && !status.getDescricao().isBlank()) {
            OcorrenciaStatusHistorico historico = new OcorrenciaStatusHistorico();
            historico.setStatusId(status.getId());
            historico.setDescricao(status.getDescricao());
            historico.setOrigem("MOSIA");
            ocorrencia.setHistoricoStatus(new ArrayList<>(java.util.List.of(historico)));
        }
    }

    private static Solicitante solicitante(MosiaRequestDTO.Pessoa pessoa) {
        Solicitante destino = new Solicitante();
        destino.setNome(pessoa.getNome()); destino.setChaveUnica(pessoa.getChaveUnica());
        destino.setEmail(pessoa.getEmail()); destino.setTelefone(pessoa.getTelefone());
        return destino;
    }

    private static Beneficiario beneficiario(MosiaRequestDTO.Pessoa pessoa) {
        Beneficiario destino = new Beneficiario();
        destino.setNome(pessoa.getNome()); destino.setChaveUnica(pessoa.getChaveUnica());
        destino.setEmail(pessoa.getEmail()); destino.setTelefone(pessoa.getTelefone());
        return destino;
    }

    private static java.time.LocalDateTime converterData(String valor) {
        if (valor == null || valor.isBlank()) return java.time.LocalDateTime.now(ZONE_ID);
        try { return OffsetDateTime.parse(valor).atZoneSameInstant(ZONE_ID).toLocalDateTime(); }
        catch (Exception ignored) { return java.time.LocalDateTime.now(ZONE_ID); }
    }
}
