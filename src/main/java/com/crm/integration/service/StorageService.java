package com.crm.integration.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.file.*;
import java.util.UUID;
import java.net.URI;
import java.io.IOException;

@Service
public class StorageService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String downloadAndSave(String fileUrl, String folder) {
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    URI.create(fileUrl),
                    HttpMethod.GET,
                    null,
                    byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                byte[] bytes = response.getBody();

                // Gera nome único
                String fileName = UUID.randomUUID().toString() + "-"
                        + Paths.get(new URI(fileUrl).getPath()).getFileName();
                String localPath = folder + "/" + fileName;

                // Cria diretório se não existir
                Files.createDirectories(Paths.get(folder));

                // Salva no disco
                Files.write(Paths.get(localPath), bytes);

                return "/uploads/autorizacoes/" + fileName;
            } else {
                throw new RuntimeException("Falha ao baixar arquivo: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar arquivo do S3: " + fileUrl, e);
        }
    }
}
