package com.crm.integration.scheduler;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import com.crm.integration.service.MensageriaService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MensageriaSchedulerTests {

    @Test
    void deveCopiarIdFuncionalidadeDaFilaParaORequest() {
        AgMensAppRepository repository = mock(AgMensAppRepository.class);
        MensageriaService service = mock(MensageriaService.class);
        MensageriaScheduler scheduler = new MensageriaScheduler(repository, service);

        AgMensApp mensagem = new AgMensApp();
        mensagem.setId(10L);
        mensagem.setChaveUnica("82459010249");
        mensagem.setMensagem("Mensagem de teste");
        mensagem.setIdFuncionalidade("31");
        mensagem.setDtInclusao(LocalDateTime.now());
        mensagem.setProc("N");

        when(repository.findById(10L)).thenReturn(Optional.of(mensagem));
        when(service.enviarParaMosia(org.mockito.ArgumentMatchers.any())).thenReturn(true);

        ReflectionTestUtils.invokeMethod(scheduler, "processarMensagem", mensagem);

        ArgumentCaptor<MensagemRequest> captor = ArgumentCaptor.forClass(MensagemRequest.class);
        verify(service).enviarParaMosia(captor.capture());
        assertEquals("31", captor.getValue().getIdFuncionalidade());
    }
}
