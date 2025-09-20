package com.crm.integration.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.file.*;
import java.util.UUID;
import java.net.URI;
import java.io.IOException;

// @Service
// public class StorageService {

//     private final RestTemplate rest = new RestTemplate();

//     /**
//      * Baixa URL (assinada) e salva em disco.
//      * Retorna caminho local do arquivo salvo.
//      */
//     public String downloadAndSave(String url, String folder) throws IOException {
//         byte[] content = rest.getForObject(url, byte[].class);
//         if (content == null)
//             throw new IOException("Arquivo vazio ou erro ao baixar: " + url);

//         URI uri = URI.create(url);
//         String rawName = Paths.get(uri.getPath()).getFileName().toString();
//         String fileName = System.currentTimeMillis() + "_" + rawName;

//         Path dir = Paths.get(folder);
//         if (Files.notExists(dir))
//             Files.createDirectories(dir);

//         Path target = dir.resolve(fileName);
//         Files.write(target, content, StandardOpenOption.CREATE);

//         return target.toAbsolutePath().toString();
//     }
// }
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

                return localPath;
            } else {
                throw new RuntimeException("Falha ao baixar arquivo: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar arquivo do S3: " + fileUrl, e);
        }
    }
}
