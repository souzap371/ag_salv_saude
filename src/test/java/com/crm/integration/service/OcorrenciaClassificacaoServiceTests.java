package com.crm.integration.service;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.enums.TipoNegocioOcorrencia;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaTipoConfiguracao;
import com.crm.integration.repository.OcorrenciaTipoConfiguracaoRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OcorrenciaClassificacaoServiceTests {

    @Test
    void deveEscolherConfiguracaoMaisEspecifica() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        OcorrenciaTipoConfiguracao generica = configuracao(TipoNegocioOcorrencia.OUTRA.name(), "9999.00069", null, null);
        OcorrenciaTipoConfiguracao autorizacao = configuracao(TipoNegocioOcorrencia.SOLICITACAO_AUTORIZACAO.name(), "9999.00069", 1895, 173);
        when(repository.findByAtivoTrue()).thenReturn(List.of(generica, autorizacao));

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdFuncionalidade("9999.00069");
        ocorrencia.setIdTipoOcorrencia(1895);
        ocorrencia.setIdAssunto(173);

        String resultado = new OcorrenciaClassificacaoService(repository).classificar(ocorrencia);

        assertEquals(TipoNegocioOcorrencia.SOLICITACAO_AUTORIZACAO.name(), resultado);
        assertEquals(resultado, ocorrencia.getTipoNegocio());
    }

    @Test
    void deveUsarOutraQuandoNaoHouverConfiguracao() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        when(repository.findByAtivoTrue()).thenReturn(List.of());
        Ocorrencia ocorrencia = new Ocorrencia();

        assertEquals(TipoNegocioOcorrencia.OUTRA.name(),
                new OcorrenciaClassificacaoService(repository).classificar(ocorrencia));
    }

    @Test
    void deveDescobrirReembolsoERegistrarCodigoDaFuncionalidade() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        when(repository.findByAtivoTrue()).thenReturn(List.of());
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdFuncionalidade("9999.00063");
        ocorrencia.setIdTipoOcorrencia(2002);
        ocorrencia.setIdAssunto(301);
        MosiaRequestDTO payload = payloadComStatus("Solicitação de reembolso registrada aguardando análise");

        String resultado = new OcorrenciaClassificacaoService(repository)
                .classificarERegistrar(ocorrencia, payload);

        assertEquals(TipoNegocioOcorrencia.REEMBOLSO.name(), resultado);
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(config ->
                TipoNegocioOcorrencia.REEMBOLSO.name().equals(config.getTipoNegocio())
                        && "9999.00063".equals(config.getIdFuncionalidade())
                        && Integer.valueOf(2002).equals(config.getIdTipoOcorrencia())
                        && Integer.valueOf(301).equals(config.getIdAssunto())));
    }

    @Test
    void deveDescobrirAtualizacaoCadastralPeloNomeDoFormulario() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        when(repository.findByAtivoTrue()).thenReturn(List.of());
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdFuncionalidade("9999.00065");
        MosiaRequestDTO payload = new MosiaRequestDTO();
        MosiaRequestDTO.OcorrenciaMosia mosia = new MosiaRequestDTO.OcorrenciaMosia();
        MosiaRequestDTO.Solicitacao solicitacao = new MosiaRequestDTO.Solicitacao();
        MosiaRequestDTO.FormularioPayload formulario = new MosiaRequestDTO.FormularioPayload();
        formulario.setNome("Atualização cadastral do beneficiário");
        solicitacao.setFormularios(List.of(formulario));
        mosia.setSolicitacao(solicitacao);
        payload.setOcorrencia(mosia);

        String resultado = new OcorrenciaClassificacaoService(repository)
                .classificarERegistrar(ocorrencia, payload);

        assertEquals(TipoNegocioOcorrencia.ATUALIZACAO_CADASTRAL.name(), resultado);
        verify(repository).save(org.mockito.ArgumentMatchers.any(OcorrenciaTipoConfiguracao.class));
    }

    @Test
    void deveManterOutraQuandoCallbackNaoTrouxerNenhumaNomenclatura() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        when(repository.findByAtivoTrue()).thenReturn(List.of());
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdFuncionalidade("9999.00999");

        String resultado = new OcorrenciaClassificacaoService(repository)
                .classificarERegistrar(ocorrencia, new MosiaRequestDTO());

        assertEquals(TipoNegocioOcorrencia.OUTRA.name(), resultado);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void configuracaoManualDeveTerPrioridadeSobreTextoDoCallback() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        OcorrenciaTipoConfiguracao manual = configuracao(
                TipoNegocioOcorrencia.ATUALIZACAO_CADASTRAL.name(), "9999.00065", null, null);
        when(repository.findByAtivoTrue()).thenReturn(List.of(manual));
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdFuncionalidade("9999.00065");

        String resultado = new OcorrenciaClassificacaoService(repository)
                .classificarERegistrar(ocorrencia, payloadComStatus("Solicitação de reembolso"));

        assertEquals(TipoNegocioOcorrencia.ATUALIZACAO_CADASTRAL.name(), resultado);
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveCompletarIdsAusentesDaAssociacaoBaseadaEmFuncionalidade() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        OcorrenciaTipoConfiguracao existente = configuracao(
                TipoNegocioOcorrencia.REEMBOLSO.name(), "9999.000663", null, null);
        when(repository.findByAtivoTrue()).thenReturn(List.of(existente));
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdFuncionalidade("9999.000663");
        ocorrencia.setIdTipoOcorrencia(1901);
        ocorrencia.setIdAssunto(77);

        String resultado = new OcorrenciaClassificacaoService(repository)
                .classificarERegistrar(ocorrencia, payloadComStatus("Reembolso"));

        assertEquals(TipoNegocioOcorrencia.REEMBOLSO.name(), resultado);
        assertEquals(1901, existente.getIdTipoOcorrencia());
        assertEquals(77, existente.getIdAssunto());
        verify(repository).save(existente);
    }

    @Test
    void devePreservarNomenclaturaDesconhecidaRecebidaDoMosia() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        when(repository.findByAtivoTrue()).thenReturn(List.of());
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdFuncionalidade("9999.00123");
        MosiaRequestDTO payload = new MosiaRequestDTO();
        MosiaRequestDTO.OcorrenciaMosia mosia = new MosiaRequestDTO.OcorrenciaMosia();
        MosiaRequestDTO.Referencia tipoMosia = new MosiaRequestDTO.Referencia();
        tipoMosia.setDescricao("Segunda via de carteirinha");
        mosia.setTipoOcorrencia(tipoMosia);
        payload.setOcorrencia(mosia);

        String resultado = new OcorrenciaClassificacaoService(repository)
                .classificarERegistrar(ocorrencia, payload);

        assertEquals("Segunda via de carteirinha", resultado);
        assertEquals(resultado, ocorrencia.getTipoNegocio());
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(config ->
                "Segunda via de carteirinha".equals(config.getTipoNegocio())
                        && "9999.00123".equals(config.getIdFuncionalidade())));
    }

    private MosiaRequestDTO payloadComStatus(String descricao) {
        MosiaRequestDTO payload = new MosiaRequestDTO();
        MosiaRequestDTO.OcorrenciaMosia mosia = new MosiaRequestDTO.OcorrenciaMosia();
        MosiaRequestDTO.OcorrenciaMosia.Atendimento atendimento =
                new MosiaRequestDTO.OcorrenciaMosia.Atendimento();
        atendimento.setStatus(new MosiaRequestDTO.OcorrenciaMosia.Atendimento.Status(descricao));
        mosia.setAtendimento(atendimento);
        payload.setOcorrencia(mosia);
        return payload;
    }

    private OcorrenciaTipoConfiguracao configuracao(
            String tipo, String funcionalidade, Integer tipoOcorrencia, Integer assunto) {
        OcorrenciaTipoConfiguracao config = new OcorrenciaTipoConfiguracao();
        config.setTipoNegocio(tipo);
        config.setIdFuncionalidade(funcionalidade);
        config.setIdTipoOcorrencia(tipoOcorrencia);
        config.setIdAssunto(assunto);
        return config;
    }
}
