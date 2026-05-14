package com.crm.integration.mapper;

import com.crm.integration.model.AutorizacaoArquivo;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        if (!ocorrencia.path("idOcorrencia").isMissingNode()) {
            autorizacao.setMosiaOcorrenciaId(
                    ocorrencia.path("idOcorrencia").asLong());
        }
        JsonNode solicitacao = ocorrencia.path("solicitacao");
        JsonNode formulario = solicitacao.path("formularios").get(0); // pegando o primeiro formulário
        JsonNode assunto = ocorrencia.path("assunto");

        autorizacao.setProtocolo(ocorrencia.path("protocolo").asText());
        autorizacao.setIdTipoGuia(assunto.path("id").asText());
        autorizacao.setTipoGuia(assunto.path("descricao").asText());
        autorizacao.setLabel(formulario.path("nome").asText());

        // === Beneficiário ===
        JsonNode beneficiario = ocorrencia.path("beneficiario");
        if (!beneficiario.isMissingNode()) {
            autorizacao.setChaveUnica(beneficiario.path("chaveUnica").asText());
            autorizacao.setNumeroContrato(beneficiario.path("numeroContrato").asText());
        }

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
        if (!statusNode.isMissingNode() && !statusNode.isNull()) {
            // descreve (texto)
            autorizacao.setStatus(statusNode.path("descricao").asText(null));

            // id pode ser número ou string -> pegar como texto seguro
            JsonNode idNode = statusNode.path("id");
            if (!idNode.isMissingNode() && !idNode.isNull()) {
                autorizacao.setStatusId(idNode.asText()); // ex: "4112"
            } else {
                autorizacao.setStatusId(null);
            }
        }

        List<AutorizacaoArquivo> arquivos = new ArrayList<>();

        JsonNode formularios = solicitacao.path("formularios");

        if (formularios.isArray()) {
            for (JsonNode form : formularios) {

                JsonNode itensArray = form.path("itens");

                if (itensArray.isArray()) {
                    for (JsonNode item : itensArray) {

                        JsonNode imagens = item.path("arquivo");

                        if (imagens.isArray()) {
                            for (JsonNode img : imagens) {

                                AutorizacaoArquivo arquivo = new AutorizacaoArquivo();
                                arquivo.setNomeOriginal(img.path("name").asText());
                                arquivo.setUrlOriginal(img.path("url").asText());

                                arquivo.setAutorizacao(autorizacao);

                                arquivos.add(arquivo);
                            }
                        }
                    }
                }
            }
        }

        autorizacao.setArquivos(arquivos);

        return autorizacao;
    }
}
