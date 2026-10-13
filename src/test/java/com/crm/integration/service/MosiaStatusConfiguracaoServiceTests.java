package com.crm.integration.service;

import com.crm.integration.dto.MosiaStatusConfigRequest;
import com.crm.integration.model.MosiaStatusConfiguracao;
import com.crm.integration.repository.MosiaStatusConfiguracaoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MosiaStatusConfiguracaoServiceTests {

    @Test
    void registraAutomaticamenteStatusRecebidoDoMosia() {
        MosiaStatusConfiguracaoRepository repository = mock(MosiaStatusConfiguracaoRepository.class);
        when(repository.findByIdStatusMosiaAndIdTipoOcorrenciaMosia(5593, 1895))
                .thenReturn(Optional.empty());
        MosiaStatusConfiguracaoService service = new MosiaStatusConfiguracaoService(repository);

        service.registrarObservado(1895, 5593, "Novo status");

        ArgumentCaptor<MosiaStatusConfiguracao> captor = ArgumentCaptor.forClass(MosiaStatusConfiguracao.class);
        verify(repository).save(captor.capture());
        MosiaStatusConfiguracao salvo = captor.getValue();
        assertEquals(5593, salvo.getIdStatusMosia());
        assertEquals(1895, salvo.getIdTipoOcorrenciaMosia());
        assertEquals("Novo status", salvo.getDescricao());
        assertEquals("MOSIA", salvo.getOrigem());
        assertNotNull(salvo.getPrimeiraIdentificacao());
        assertNotNull(salvo.getUltimaIdentificacao());
    }

    @Test
    void atualizaDescricaoQuandoStatusJaExiste() {
        MosiaStatusConfiguracao existente = new MosiaStatusConfiguracao();
        existente.setIdStatusMosia(5593);
        existente.setIdTipoOcorrenciaMosia(1895);
        existente.setDescricao("Descrição antiga");
        existente.setOrigem("MOSIA");
        MosiaStatusConfiguracaoRepository repository = mock(MosiaStatusConfiguracaoRepository.class);
        when(repository.findByIdStatusMosiaAndIdTipoOcorrenciaMosia(5593, 1895))
                .thenReturn(Optional.of(existente));
        MosiaStatusConfiguracaoService service = new MosiaStatusConfiguracaoService(repository);

        service.registrarObservado(1895, 5593, "Descrição atualizada");

        assertEquals("Descrição atualizada", existente.getDescricao());
        assertNotNull(existente.getUltimaIdentificacao());
        verify(repository).save(existente);
    }

    @Test
    void criaStatusPeloPainel() {
        MosiaStatusConfiguracaoRepository repository = mock(MosiaStatusConfiguracaoRepository.class);
        when(repository.findByIdStatusMosiaAndIdTipoOcorrenciaMosia(5593, 1895))
                .thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MosiaStatusConfiguracaoService service = new MosiaStatusConfiguracaoService(repository);

        var resposta = service.criar(new MosiaStatusConfigRequest(5593, 1895, "Novo status", true));

        assertEquals(5593, resposta.idStatusMosia());
        assertEquals(1895, resposta.idTipoOcorrenciaMosia());
        assertEquals("PAINEL", resposta.origem());
    }

    @Test
    void rejeitaStatusDuplicadoNoMesmoTipo() {
        MosiaStatusConfiguracaoRepository repository = mock(MosiaStatusConfiguracaoRepository.class);
        when(repository.findByIdStatusMosiaAndIdTipoOcorrenciaMosia(5593, 1895))
                .thenReturn(Optional.of(new MosiaStatusConfiguracao()));
        MosiaStatusConfiguracaoService service = new MosiaStatusConfiguracaoService(repository);

        ResponseStatusException erro = assertThrows(ResponseStatusException.class, () ->
                service.criar(new MosiaStatusConfigRequest(5593, 1895, "Duplicado", true)));

        assertEquals(409, erro.getStatusCode().value());
    }

    @Test
    void encontraIdAtivoPelaDescricao() {
        MosiaStatusConfiguracao status = new MosiaStatusConfiguracao();
        status.setIdStatusMosia(5593);
        status.setIdTipoOcorrenciaMosia(1895);
        status.setDescricao("Novo status");
        status.setAtivo(true);
        MosiaStatusConfiguracaoRepository repository = mock(MosiaStatusConfiguracaoRepository.class);
        when(repository.findAll()).thenReturn(List.of(status));
        MosiaStatusConfiguracaoService service = new MosiaStatusConfiguracaoService(repository);

        assertEquals(5593, service.buscarCodigoPorDescricao("novo STATUS"));
    }
}
