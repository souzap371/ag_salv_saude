package com.crm.integration.mapper;

import com.crm.integration.dto.MosiaRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OcorrenciaMapperTests {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void usaChaveUnicaComoIdTextualDoBeneficiario() {
        MosiaRequestDTO.Pessoa beneficiario = new MosiaRequestDTO.Pessoa();
        beneficiario.setChaveUnica("BEN-00123");
        beneficiario.setNome("Beneficiário de teste");

        MosiaRequestDTO.OcorrenciaMosia payload = new MosiaRequestDTO.OcorrenciaMosia();
        payload.setBeneficiario(beneficiario);

        var ocorrencia = OcorrenciaMapper.fromMosia(payload);

        assertThat(ocorrencia.getBeneficiario().getId()).isEqualTo("BEN-00123");
    }

    @Test
    void rejeitaChaveUnicaAusenteComoIdDoBeneficiario() {
        MosiaRequestDTO.Pessoa beneficiario = new MosiaRequestDTO.Pessoa();
        beneficiario.setChaveUnica("  ");

        MosiaRequestDTO.OcorrenciaMosia payload = new MosiaRequestDTO.OcorrenciaMosia();
        payload.setBeneficiario(beneficiario);

        assertThatThrownBy(() -> OcorrenciaMapper.fromMosia(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("é obrigatória");
    }

    @Test
    void preencheDadosAusentesDoBeneficiarioComItensDoFormulario() throws Exception {
        MosiaRequestDTO.Pessoa beneficiario = new MosiaRequestDTO.Pessoa();
        beneficiario.setChaveUnica("82459010249");

        MosiaRequestDTO.FormularioPayload formulario = new MosiaRequestDTO.FormularioPayload();
        formulario.setItens(JSON.readTree("""
                [{
                  "nome": "Gabriella da Rocha Batista",
                  "telefone-celular": "69992080601",
                  "e-mail": "teste@example.com"
                }]
                """));
        MosiaRequestDTO.Solicitacao solicitacao = new MosiaRequestDTO.Solicitacao();
        solicitacao.setFormularios(List.of(formulario));

        MosiaRequestDTO.OcorrenciaMosia payload = new MosiaRequestDTO.OcorrenciaMosia();
        payload.setBeneficiario(beneficiario);
        payload.setSolicitacao(solicitacao);

        var ocorrencia = OcorrenciaMapper.fromMosia(payload);

        assertThat(ocorrencia.getBeneficiario().getNome())
                .isEqualTo("Gabriella da Rocha Batista");
        assertThat(ocorrencia.getBeneficiario().getTelefone()).isEqualTo("69992080601");
        assertThat(ocorrencia.getBeneficiario().getEmail()).isEqualTo("teste@example.com");
    }

    @Test
    void mantemDadosDiretosDoBeneficiarioQuandoFormularioTambemPossuiValores() throws Exception {
        MosiaRequestDTO.Pessoa beneficiario = new MosiaRequestDTO.Pessoa();
        beneficiario.setChaveUnica("82459010249");
        beneficiario.setNome("Nome direto do Mosia");
        beneficiario.setEmail("direto@example.com");

        MosiaRequestDTO.FormularioPayload formulario = new MosiaRequestDTO.FormularioPayload();
        formulario.setItens(JSON.readTree("""
                [{"nome":"Nome do formulário","e-mail":"formulario@example.com"}]
                """));
        MosiaRequestDTO.Solicitacao solicitacao = new MosiaRequestDTO.Solicitacao();
        solicitacao.setFormularios(List.of(formulario));

        MosiaRequestDTO.OcorrenciaMosia payload = new MosiaRequestDTO.OcorrenciaMosia();
        payload.setBeneficiario(beneficiario);
        payload.setSolicitacao(solicitacao);

        var ocorrencia = OcorrenciaMapper.fromMosia(payload);

        assertThat(ocorrencia.getBeneficiario().getNome()).isEqualTo("Nome direto do Mosia");
        assertThat(ocorrencia.getBeneficiario().getEmail()).isEqualTo("direto@example.com");
    }
}
