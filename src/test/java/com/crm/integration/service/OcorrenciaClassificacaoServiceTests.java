package com.crm.integration.service;

import com.crm.integration.enums.TipoNegocioOcorrencia;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaTipoConfiguracao;
import com.crm.integration.repository.OcorrenciaTipoConfiguracaoRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OcorrenciaClassificacaoServiceTests {

    @Test
    void deveEscolherConfiguracaoMaisEspecifica() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        OcorrenciaTipoConfiguracao generica = configuracao(TipoNegocioOcorrencia.OUTRA, "9999.00069", null, null);
        OcorrenciaTipoConfiguracao autorizacao = configuracao(TipoNegocioOcorrencia.SOLICITACAO_AUTORIZACAO, "9999.00069", 1895, 173);
        when(repository.findByAtivoTrue()).thenReturn(List.of(generica, autorizacao));

        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setIdFuncionalidade("9999.00069");
        ocorrencia.setIdTipoOcorrencia(1895);
        ocorrencia.setIdAssunto(173);

        TipoNegocioOcorrencia resultado = new OcorrenciaClassificacaoService(repository).classificar(ocorrencia);

        assertEquals(TipoNegocioOcorrencia.SOLICITACAO_AUTORIZACAO, resultado);
        assertEquals(resultado, ocorrencia.getTipoNegocio());
    }

    @Test
    void deveUsarOutraQuandoNaoHouverConfiguracao() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        when(repository.findByAtivoTrue()).thenReturn(List.of());
        Ocorrencia ocorrencia = new Ocorrencia();

        assertEquals(TipoNegocioOcorrencia.OUTRA,
                new OcorrenciaClassificacaoService(repository).classificar(ocorrencia));
    }

    private OcorrenciaTipoConfiguracao configuracao(
            TipoNegocioOcorrencia tipo, String funcionalidade, Integer tipoOcorrencia, Integer assunto) {
        OcorrenciaTipoConfiguracao config = new OcorrenciaTipoConfiguracao();
        config.setTipoNegocio(tipo);
        config.setIdFuncionalidade(funcionalidade);
        config.setIdTipoOcorrencia(tipoOcorrencia);
        config.setIdAssunto(assunto);
        return config;
    }
}
