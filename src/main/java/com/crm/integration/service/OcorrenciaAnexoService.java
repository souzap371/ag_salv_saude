package com.crm.integration.service;

import com.crm.integration.model.Formulario;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaArquivo;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;

@Service
public class OcorrenciaAnexoService {
    private final StorageService storageService;

    public OcorrenciaAnexoService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void importarAnexosDosFormularios(Ocorrencia ocorrencia) {
        if (ocorrencia == null || ocorrencia.getFormularios() == null) return;
        if (ocorrencia.getArquivos() == null) ocorrencia.setArquivos(new ArrayList<>());
        Set<String> urlsProcessadas = new HashSet<>();
        for (Formulario formulario : ocorrencia.getFormularios()) {
            JsonNode dados = formulario.getFormDataJson();
            if (dados != null) percorrer(ocorrencia, dados, formulario.getIdForm(), urlsProcessadas);
        }
    }

    private void percorrer(Ocorrencia ocorrencia, JsonNode node, String caminho, Set<String> urlsProcessadas) {
        if (node == null || node.isNull()) return;
        if (node.isObject()) {
            JsonNode url = node.get("url");
            if (url != null && url.isTextual() && urlsProcessadas.add(url.asText())) {
                adicionarArquivo(ocorrencia, node, caminho);
            }
            node.fields().forEachRemaining(entry ->
                    percorrer(ocorrencia, entry.getValue(), caminho + "/" + entry.getKey(), urlsProcessadas));
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) percorrer(ocorrencia, node.get(i), caminho + "/" + i, urlsProcessadas);
        }
    }

    private void adicionarArquivo(Ocorrencia ocorrencia, JsonNode metadados, String caminho) {
        StorageService.ArquivoBaixado baixado = storageService.download(metadados.path("url").asText());
        OcorrenciaArquivo arquivo = new OcorrenciaArquivo();
        String nome = metadados.path("name").asText(null);
        arquivo.setNomeOriginal(nome == null || nome.isBlank() ? baixado.nome() : nome);
        arquivo.setTipoMime(baixado.tipoMime());
        arquivo.setCampoOrigem(caminho);
        arquivo.setConteudo(baixado.conteudo());
        arquivo.setHashSha256(sha256(baixado.conteudo()));
        arquivo.setOcorrencia(ocorrencia);
        ocorrencia.getArquivos().add(arquivo);
    }

    private String sha256(byte[] conteudo) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(conteudo));
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível calcular o hash do anexo.", ex);
        }
    }
}
