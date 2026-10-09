package com.crm.integration.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StorageServiceTests {

    @Test
    void baixaAnexoHttpsSemExigirListaDeHosts() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        StorageService storageService = new StorageService(restTemplate, 10_485_760L);
        byte[] conteudo = "conteudo-pdf".getBytes();
        String url = "https://arquivos.exemplo.test/laudo-medico.pdf";

        server.expect(requestTo(url))
                .andRespond(withSuccess(conteudo, MediaType.APPLICATION_PDF));

        StorageService.ArquivoBaixado arquivo = storageService.download(url);

        assertThat(arquivo.nome()).isEqualTo("laudo-medico.pdf");
        assertThat(arquivo.tipoMime()).isEqualTo(MediaType.APPLICATION_PDF_VALUE);
        assertThat(arquivo.conteudo()).isEqualTo(conteudo);
        server.verify();
    }

    @Test
    void continuaRecusandoUrlSemHttps() {
        StorageService storageService = new StorageService(new RestTemplate(), 10_485_760L);

        assertThatThrownBy(() -> storageService.download("http://arquivos.exemplo.test/anexo.pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A URL do anexo deve utilizar HTTPS.");
    }
}
