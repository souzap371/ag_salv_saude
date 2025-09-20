package com.crm.integration.mapper;

import com.crm.integration.model.AutorizacaoArquivo;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class AutorizacaoMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AutorizacaoSolicitada mapCallbackToEntity(String callbackJson) throws Exception {
        JsonNode root = objectMapper.readTree(callbackJson);

        AutorizacaoSolicitada autorizacao = new AutorizacaoSolicitada();

        // Armazena o JSON cru
        autorizacao.setRawCallbackJson(callbackJson);

        // Campos principais
        JsonNode ocorrencia = root.path("ocorrencia");
        JsonNode solicitacao = ocorrencia.path("solicitacao");
        JsonNode formulario = solicitacao.path("formularios").get(0); // pegando o primeiro formulário
        JsonNode assunto = ocorrencia.path("assunto");

        autorizacao.setProtocolo(ocorrencia.path("protocolo").asText());
        autorizacao.setIdTipoGuia(assunto.path("id").asText());
        autorizacao.setTipoGuia(assunto.path("descricao").asText());
        autorizacao.setLabel(formulario.path("nome").asText());

        // Campos do contato
        JsonNode itens = formulario.path("itens").get(0); // pegando o primeiro item
        autorizacao.setNome(itens.path("nome-do-contato").asText());
        autorizacao.setTelefoneContato(itens.path("celular-do-contato").asText());
        autorizacao.setEmailContato(
                itens.has("email") ? itens.path("email").asText() : itens.path("email-do-contato").asText());

        // autorizacao.setEmailContato(itens.path("email-do-contato").asText());
        autorizacao.setObservacao(itens.path("observacao").asText());

        // Status inicial
        JsonNode statusNode = ocorrencia.path("atendimento").path("status");
        if (!statusNode.isMissingNode()) {
            autorizacao.setStatus(statusNode.path("descricao").asText());
        }

        // Arquivos
        List<AutorizacaoArquivo> arquivos = new ArrayList<>();
        JsonNode imagens = itens.path("imagem-da-guia-solicitacao");
        if (imagens.isArray()) {
            for (JsonNode img : imagens) {
                AutorizacaoArquivo arquivo = new AutorizacaoArquivo();
                arquivo.setNomeOriginal(img.path("name").asText());
                arquivo.setUrlOriginal(img.path("url").asText());
                arquivo.setCaminhoLocal(null); // preencha se você salvar localmente
                arquivo.setBaixadoEm(OffsetDateTime.now());
                arquivo.setAutorizacao(autorizacao); // importante para o relacionamento
                arquivos.add(arquivo);
            }
        }
        autorizacao.setArquivos(arquivos);

        return autorizacao;
    }
}
