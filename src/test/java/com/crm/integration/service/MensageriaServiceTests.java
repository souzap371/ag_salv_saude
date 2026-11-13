package com.crm.integration.service;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MensageriaServiceTests {

    @Test
    void naoDeveEnviarIdFuncionalidadeQuandoNaoPreenchido() throws Exception {
        MensagemRequest request = new MensagemRequest();
        request.setChaveUnica("82459010249");
        request.setMsg("Mensagem de teste");

        String json = new ObjectMapper().writeValueAsString(request);

        assertFalse(json.contains("idFuncionalidade"));
    }

    @Test
    void deveEnviarIdFuncionalidadeNoPayloadDoMosia() {
        AgMensAppRepository repository = mock(AgMensAppRepository.class);
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
        MensageriaService service = new MensageriaService(repository, restTemplate);
        ReflectionTestUtils.setField(service, "mosiaApiUrl", "https://mosia.test/mensageria");
        ReflectionTestUtils.setField(service, "mosiaApiToken", "token-teste");

        server.expect(requestTo("https://mosia.test/mensageria"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "token-teste"))
                .andExpect(content().json("""
                        {
                          "chaveUnica": "82459010249",
                          "msg": "Mensagem de teste",
                          "idFuncionalidade": "31"
                        }
                        """))
                .andRespond(withSuccess());

        MensagemRequest request = new MensagemRequest();
        request.setChaveUnica("82459010249");
        request.setMsg("Mensagem de teste");
        request.setIdFuncionalidade("31");

        assertTrue(service.enviarParaMosia(request));
        server.verify();
    }

    @Test
    void devePriorizarFuncionalidadeRecebidaAoSalvarMensagem() {
        AgMensAppRepository repository = mock(AgMensAppRepository.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        MensageriaService service = new MensageriaService(repository, restTemplate);
        ReflectionTestUtils.setField(service, "mosiaApiUrl", "https://mosia.test/mensageria");
        ReflectionTestUtils.setField(service, "mosiaApiToken", "token-teste");
        when(repository.save(any(AgMensApp.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.postForEntity(any(String.class), any(), any(Class.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("{}"));

        MensagemRequest request = new MensagemRequest();
        request.setChaveUnica("82459010249");
        request.setMsg("Mensagem de teste");
        request.setIdFuncionalidade("31");

        AgMensApp entity = service.salvarMensagem(request, "9999", "1");

        org.junit.jupiter.api.Assertions.assertEquals("31", entity.getIdFuncionalidade());
    }
}
