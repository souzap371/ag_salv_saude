// package com.crm.integration.mapper;

// import com.crm.integration.dto.*;
// import com.crm.integration.model.*;

// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;

// public class OcorrenciaMapper {

//     public static Ocorrencia fromMosia(OcorrenciaMosia mosia) {
//         Ocorrencia ocorrencia = new Ocorrencia();
//         DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

//         ocorrencia.setProtocolo(mosia.getProtocolo());
//         ocorrencia.setDataRegistro(LocalDateTime.parse(mosia.getDataRegistro(), fmt));
//         ocorrencia.setDataAtualizacao(LocalDateTime.parse(mosia.getDataAtualizacao(), fmt));
//         ocorrencia.setIdTipoOcorrencia(mosia.getTipoOcorrencia().getId());
//         ocorrencia.setIdAssunto(mosia.getAssunto().getId());
//         ocorrencia.setNumeroContrato(mosia.getSolicitante().getNumeroContrato());

//         // Solicitante
//         Solicitante solicitante = new Solicitante();
//         solicitante.setChaveUnica(mosia.getSolicitante().getChaveUnica());
//         ocorrencia.setSolicitante(solicitante);

//         // Beneficiário
//         Beneficiario beneficiario = new Beneficiario();
//         beneficiario.setChaveUnica(mosia.getBeneficiario().getChaveUnica());
//         ocorrencia.setBeneficiario(beneficiario);

//         return ocorrencia;
//     }
// }


package com.crm.integration.mapper;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.model.Ocorrencia;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class OcorrenciaMapper {

    /**
     * Converte o DTO recebido do Mosia (nova estrutura) para a entidade Ocorrencia
     */
    public static Ocorrencia fromMosia(MosiaRequestDTO.OcorrenciaMosia dto) {
        if (dto == null) {
            return null;
        }

        Ocorrencia ocorrencia = new Ocorrencia();

        // Protocolo
        ocorrencia.setProtocolo(dto.getProtocolo());

        // Status
        if (dto.getAtendimento() != null && dto.getAtendimento().getStatus() != null) {
            ocorrencia.setStatus(dto.getAtendimento().getStatus().getDescricao());
        }

        // Define data de atualização como agora, já que o DTO não traz datas
        ocorrencia.setDataAtualizacao(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")));

        /**
         * ⚠️ Os campos abaixo estavam no seu mapper original, porém NÃO EXISTEM no novo DTO enviado.
         * Assim, deixei comentado — caso queira reativar, você precisa adicionar esses campos ao DTO.
         */

        /*
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;

        if (dto.getDataRegistro() != null)
            ocorrencia.setDataRegistro(LocalDateTime.parse(dto.getDataRegistro(), fmt));

        if (dto.getDataAtualizacao() != null)
            ocorrencia.setDataAtualizacao(LocalDateTime.parse(dto.getDataAtualizacao(), fmt));

        if (dto.getTipoOcorrencia() != null)
            ocorrencia.setIdTipoOcorrencia(dto.getTipoOcorrencia().getId());

        if (dto.getAssunto() != null)
            ocorrencia.setIdAssunto(dto.getAssunto().getId());

        if (dto.getSolicitante() != null)
            ocorrencia.setNumeroContrato(dto.getSolicitante().getNumeroContrato());

        // Solicitante
        if (dto.getSolicitante() != null) {
            Solicitante solicitante = new Solicitante();
            solicitante.setChaveUnica(dto.getSolicitante().getChaveUnica());
            ocorrencia.setSolicitante(solicitante);
        }

        // Beneficiário
        if (dto.getBeneficiario() != null) {
            Beneficiario beneficiario = new Beneficiario();
            beneficiario.setChaveUnica(dto.getBeneficiario().getChaveUnica());
            ocorrencia.setBeneficiario(beneficiario);
        }
        */

        return ocorrencia;
    }
}
