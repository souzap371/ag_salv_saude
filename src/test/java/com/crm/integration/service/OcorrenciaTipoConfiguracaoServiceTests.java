package com.crm.integration.service;

import com.crm.integration.dto.OcorrenciaTipoConfigRequest;
import com.crm.integration.enums.TipoNegocioOcorrencia;
import com.crm.integration.model.OcorrenciaTipoConfiguracao;
import com.crm.integration.repository.OcorrenciaTipoConfiguracaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OcorrenciaTipoConfiguracaoServiceTests {

    @Test
    void criaAssociacaoComIdsDoMosia() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        when(repository.findAll()).thenReturn(List.of());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        OcorrenciaTipoConfiguracaoService service = new OcorrenciaTipoConfiguracaoService(repository);

        var response = service.criar(new OcorrenciaTipoConfigRequest(
                TipoNegocioOcorrencia.ATUALIZACAO_CADASTRAL, 48, 128, 173, true));

        assertEquals(TipoNegocioOcorrencia.ATUALIZACAO_CADASTRAL, response.tipoNegocio());
        assertEquals(48, response.idFuncionalidade());
        verify(repository).saveAndFlush(any(OcorrenciaTipoConfiguracao.class));
    }

    @Test
    void exigePeloMenosUmId() {
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        OcorrenciaTipoConfiguracaoService service = new OcorrenciaTipoConfiguracaoService(repository);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class, () ->
                service.criar(new OcorrenciaTipoConfigRequest(
                        TipoNegocioOcorrencia.ATUALIZACAO_CADASTRAL, null, null, null, true)));

        assertEquals(400, erro.getStatusCode().value());
    }

    @Test
    void recusaAssociacaoDuplicada() {
        OcorrenciaTipoConfiguracao existente = new OcorrenciaTipoConfiguracao();
        existente.setTipoNegocio(TipoNegocioOcorrencia.REEMBOLSO);
        existente.setIdAssunto(99);
        OcorrenciaTipoConfiguracaoRepository repository = mock(OcorrenciaTipoConfiguracaoRepository.class);
        when(repository.findAll()).thenReturn(List.of(existente));
        OcorrenciaTipoConfiguracaoService service = new OcorrenciaTipoConfiguracaoService(repository);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class, () ->
                service.criar(new OcorrenciaTipoConfigRequest(
                        TipoNegocioOcorrencia.REEMBOLSO, null, null, 99, true)));

        assertEquals(409, erro.getStatusCode().value());
    }
}
