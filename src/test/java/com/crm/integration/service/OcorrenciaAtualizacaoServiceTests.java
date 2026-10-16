package com.crm.integration.service;

import com.crm.integration.model.Beneficiario;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.repository.AgMensAppRepository;
import com.crm.integration.repository.OcorrenciaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OcorrenciaAtualizacaoServiceTests {

    @Test
    void deveAtualizarTipoDesconhecidoEEnfileirarMensagemGenerica() {
        OcorrenciaRepository ocorrencias = mock(OcorrenciaRepository.class);
        AgMensAppRepository mensagens = mock(AgMensAppRepository.class);
        MensageriaService mensageria = mock(MensageriaService.class);
        MosiaIntegrationService mosia = mock(MosiaIntegrationService.class);
        MosiaStatusConfiguracaoService catalogo = mock(MosiaStatusConfiguracaoService.class);
        Ocorrencia ocorrencia = ocorrencia("PROTO-1", "Segunda via de carteirinha", "Recebida");
        when(ocorrencias.findTopByProtocoloOrderByDataAtualizacaoDescIdDesc("PROTO-1"))
                .thenReturn(Optional.of(ocorrencia));
        when(mosia.atualizarStatusNoMosia("PROTO-1", 9001, "Concluída", "82459010249"))
                .thenReturn(true);

        var resultado = new OcorrenciaAtualizacaoService(
                ocorrencias, mensagens, mensageria, mosia, catalogo)
                .atualizarStatus("PROTO-1", 9001, "Concluída", null, 123456L);

        assertTrue(resultado.sincronizadoMosia());
        assertTrue(resultado.mensagemCriada());
        assertEquals("Concluída", ocorrencia.getStatus());
        assertEquals(1, ocorrencia.getHistoricoStatus().size());
        ArgumentCaptor<com.crm.integration.model.MensagemRequest> request =
                ArgumentCaptor.forClass(com.crm.integration.model.MensagemRequest.class);
        verify(mensageria).enfileirarMensagem(
                request.capture(), eq("9999.00123"), any(), eq(123456L));
        assertTrue(request.getValue().getMsg().contains("Segunda via de carteirinha"));
    }

    @Test
    void naoDeveDuplicarMensagemQuandoStatusNaoMudou() {
        OcorrenciaRepository ocorrencias = mock(OcorrenciaRepository.class);
        AgMensAppRepository mensagens = mock(AgMensAppRepository.class);
        MensageriaService mensageria = mock(MensageriaService.class);
        MosiaIntegrationService mosia = mock(MosiaIntegrationService.class);
        MosiaStatusConfiguracaoService catalogo = mock(MosiaStatusConfiguracaoService.class);
        Ocorrencia ocorrencia = ocorrencia("PROTO-2", "REEMBOLSO", "Aprovado");
        when(ocorrencias.findTopByProtocoloOrderByDataAtualizacaoDescIdDesc("PROTO-2"))
                .thenReturn(Optional.of(ocorrencia));

        var resultado = new OcorrenciaAtualizacaoService(
                ocorrencias, mensagens, mensageria, mosia, catalogo)
                .atualizarStatus("PROTO-2", 10, "Aprovado", null);

        assertTrue(!resultado.mensagemCriada());
        verify(mensageria, org.mockito.Mockito.never()).enfileirarMensagem(any(), any(), any());
    }

    private Ocorrencia ocorrencia(String protocolo, String tipo, String status) {
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setProtocolo(protocolo);
        ocorrencia.setTipoNegocio(tipo);
        ocorrencia.setStatus(status);
        ocorrencia.setIdFuncionalidade("9999.00123");
        ocorrencia.setIdTipoOcorrencia(1900);
        Beneficiario beneficiario = new Beneficiario();
        beneficiario.setId("82459010249");
        ocorrencia.setBeneficiario(beneficiario);
        return ocorrencia;
    }
}
