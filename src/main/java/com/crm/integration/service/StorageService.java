package com.crm.integration.service;

import java.net.URI;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StorageService {

    private final RestTemplate restTemplate;
    private final long maxFileSizeBytes;

    public StorageService(
            RestTemplate restTemplate,
            @Value("${storage.database.max-file-size-bytes:10485760}") long maxFileSizeBytes) {
        this.restTemplate = restTemplate;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public ArquivoBaixado download(String fileUrl) {
        URI uri = validarUrl(fileUrl);
        ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, null, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Não foi possível baixar o anexo da Mosia.");
        }

        byte[] conteudo = response.getBody();
        if (conteudo.length > maxFileSizeBytes) {
            throw new IllegalArgumentException("Anexo excede o limite permitido de " + maxFileSizeBytes + " bytes.");
        }

        String nome = nomeArquivo(response.getHeaders(), uri);
        String tipoMime = response.getHeaders().getContentType() == null
                ? "application/octet-stream"
                : response.getHeaders().getContentType().toString();

        return new ArquivoBaixado(nome, tipoMime, conteudo);
    }

    private URI validarUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("URL do anexo não informada.");
        }
        URI uri = URI.create(fileUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException("A URL do anexo deve utilizar HTTPS.");
        }
        return uri;
    }

    private String nomeArquivo(HttpHeaders headers, URI uri) {
        if (headers.getContentDisposition().getFilename() != null) {
            return headers.getContentDisposition().getFilename();
        }
        if (uri.getPath() != null && !uri.getPath().isBlank()) {
            var path = Paths.get(uri.getPath()).getFileName();
            if (path != null && !path.toString().isBlank()) return path.toString();
        }
        return "anexo";
    }

    public record ArquivoBaixado(String nome, String tipoMime, byte[] conteudo) {}
}
