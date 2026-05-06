package com.crm.integration.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.nio.file.*;
import java.util.UUID;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StorageService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${storage.path}")
    private String basePath;
    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    public String downloadAndSave(String fileUrl) {
    try {
    ResponseEntity<byte[]> response = restTemplate.exchange(
    URI.create(fileUrl),
    HttpMethod.GET,
    null,
    byte[].class);

    if (response.getStatusCode().is2xxSuccessful()) {
    byte[] bytes = response.getBody();

    String fileName = UUID.randomUUID().toString() + "-"
    + Paths.get(new URI(fileUrl).getPath()).getFileName();

    // CAMINHO MONTADO NO LINUX
    // String basePath = "/mnt/scm_guias";

    Path fullPath = Paths.get(basePath, fileName);

    Files.createDirectories(fullPath.getParent());
    Files.write(fullPath, bytes);

    // LOG
    log.info("[STORAGE] Arquivo salvo com sucesso | path={} | origem={}",
    fullPath, fileUrl);

    return fullPath.toString();

    } else {
    throw new RuntimeException("Falha ao baixar arquivo: " +
    response.getStatusCode());
    }
    } catch (Exception e) {
    throw new RuntimeException("Erro ao baixar arquivo do S3: " + fileUrl, e);
    }
    }

    public String downloadAndSave(String fileUrl, String protocolo) {
    try {
    ResponseEntity<byte[]> response = restTemplate.exchange(
    URI.create(fileUrl),
    HttpMethod.GET,
    null,
    byte[].class);

    if (response.getStatusCode().is2xxSuccessful()) {
    byte[] bytes = response.getBody();

    // String fileName = UUID.randomUUID() + "-"
    // + Paths.get(new URI(fileUrl).getPath()).getFileName();
    String fileName = Paths.get(new
    URI(fileUrl).getPath()).getFileName().toString();

    // 👇 ORGANIZA POR PROTOCOLO
    Path folderPath = Paths.get(basePath, protocolo);

    Files.createDirectories(folderPath);

    Path fullPath = folderPath.resolve(fileName);

    Files.write(fullPath, bytes);

    log.info("[STORAGE] Salvo | protocolo={} | path={}", protocolo, fullPath);

    return fullPath.toString();
    }

    throw new RuntimeException("Falha ao baixar arquivo");

    } catch (Exception e) {
    throw new RuntimeException("Erro ao baixar arquivo: " + fileUrl, e);
    }
    }

    
}
