// package com.crm.integration.mapper;

// import com.crm.integration.model.AutorizacaoArquivo;
// import com.crm.integration.model.AutorizacaoSolicitada;
// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.util.ArrayList;
// import java.util.List;

// import org.springframework.stereotype.Component;

// @Component
// public class AutorizacaoMapper {

//     private final ObjectMapper objectMapper = new ObjectMapper();

//     public AutorizacaoSolicitada mapCallbackToEntity(String callbackJson) throws Exception {
//         JsonNode root = objectMapper.readTree(callbackJson);

//         AutorizacaoSolicitada autorizacao = new AutorizacaoSolicitada();

//         // Armazena o JSON cru
//         autorizacao.setRawCallbackJson(callbackJson);

//         // Campos principais
//         JsonNode ocorrencia = root.path("ocorrencia");
//         if (!ocorrencia.path("idOcorrencia").isMissingNode()) {
//             autorizacao.setMosiaOcorrenciaId(
//                     ocorrencia.path("idOcorrencia").asLong());
//         }
//         JsonNode solicitacao = ocorrencia.path("solicitacao");
//         JsonNode formulario = solicitacao.path("formularios").get(0); // pegando o primeiro formulário
//         JsonNode assunto = ocorrencia.path("assunto");

//         autorizacao.setProtocolo(ocorrencia.path("protocolo").asText());
//         autorizacao.setIdTipoGuia(assunto.path("id").asText());
//         autorizacao.setTipoGuia(assunto.path("descricao").asText());
//         autorizacao.setLabel(formulario.path("nome").asText());

//         // === Beneficiário ===
//         JsonNode beneficiario = ocorrencia.path("beneficiario");
//         if (!beneficiario.isMissingNode()) {
//             autorizacao.setChaveUnica(beneficiario.path("chaveUnica").asText());
//             autorizacao.setNumeroContrato(beneficiario.path("numeroContrato").asText());
//         }

//         // Campos do contato
//         JsonNode itens = formulario.path("itens").get(0); // pegando o primeiro item
//         autorizacao.setNome(itens.path("nome-do-contato").asText());
//         autorizacao.setTelefoneContato(itens.path("celular-do-contato").asText());
//         autorizacao.setEmailContato(
//                 itens.has("email") ? itens.path("email").asText() : itens.path("email-do-contato").asText());

//         // autorizacao.setEmailContato(itens.path("email-do-contato").asText());
//         autorizacao.setObservacao(itens.path("observacao").asText());

//         // Status inicial
//         JsonNode statusNode = ocorrencia.path("atendimento").path("status");
//         if (!statusNode.isMissingNode() && !statusNode.isNull()) {
//             // descreve (texto)
//             autorizacao.setStatus(statusNode.path("descricao").asText(null));

//             // id pode ser número ou string -> pegar como texto seguro
//             JsonNode idNode = statusNode.path("id");
//             if (!idNode.isMissingNode() && !idNode.isNull()) {
//                 autorizacao.setStatusId(idNode.asText()); // ex: "4112"
//             } else {
//                 autorizacao.setStatusId(null);
//             }
//         }

//         List<AutorizacaoArquivo> arquivos = new ArrayList<>();

//         JsonNode formularios = solicitacao.path("formularios");

//         if (formularios.isArray()) {
//             for (JsonNode form : formularios) {

//                 JsonNode itensArray = form.path("itens");

//                 if (itensArray.isArray()) {
//                     for (JsonNode item : itensArray) {

//                         JsonNode imagens = item.path("arquivo");

//                         if (imagens.isArray()) {
//                             for (JsonNode img : imagens) {

//                                 AutorizacaoArquivo arquivo = new AutorizacaoArquivo();
//                                 arquivo.setNomeOriginal(img.path("name").asText());
//                                 arquivo.setUrlOriginal(img.path("url").asText());

//                                 arquivo.setAutorizacao(autorizacao);

//                                 arquivos.add(arquivo);
//                             }
//                         }
//                     }
//                 }
//             }
//         }

//         autorizacao.setArquivos(arquivos);

//         return autorizacao;
//     }
// }

package com.crm.integration.mapper;

import com.crm.integration.model.AutorizacaoArquivo;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AutorizacaoMapper {

    private static final Logger log = LoggerFactory.getLogger(AutorizacaoMapper.class);

    private final ObjectMapper objectMapper;

    public AutorizacaoMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converte o callback de autorização recebido da Mosia
     * para a entidade AutorizacaoSolicitada.
     *
     * A CHAVEUNICA gravada deve ser a chave do SOLICITANTE,
     * pois é esse valor que a API da Mosia exige no campo
     * chaveUnicaSolicitante durante atualizações de status.
     */
    public AutorizacaoSolicitada mapCallbackToEntity(
            String callbackJson) throws Exception {

        if (callbackJson == null || callbackJson.isBlank()) {
            throw new IllegalArgumentException(
                    "O JSON do callback não pode ser vazio.");
        }

        JsonNode root = objectMapper.readTree(callbackJson);

        JsonNode ocorrencia = root.path("ocorrencia");

        if (ocorrencia.isMissingNode()
                || ocorrencia.isNull()) {

            throw new IllegalArgumentException(
                    "O callback não possui o objeto 'ocorrencia'.");
        }

        AutorizacaoSolicitada autorizacao = new AutorizacaoSolicitada();

        autorizacao.setRawCallbackJson(
                callbackJson);

        preencherDadosPrincipais(
                autorizacao,
                ocorrencia);

        preencherSolicitante(
                autorizacao,
                ocorrencia);

        preencherBeneficiario(
                autorizacao,
                ocorrencia);

        preencherStatus(
                autorizacao,
                ocorrencia);

        preencherFormulario(
                autorizacao,
                ocorrencia);

        preencherArquivos(
                autorizacao,
                ocorrencia);

        log.info(
                "[AUTORIZACAO MAPPER] Callback mapeado. "
                        + "protocolo={} status={} statusId={} "
                        + "chaveSolicitante={}",
                autorizacao.getProtocolo(),
                autorizacao.getStatus(),
                autorizacao.getStatusId(),
                mascararChave(
                        autorizacao.getChaveUnica()));

        return autorizacao;
    }

    private void preencherDadosPrincipais(
            AutorizacaoSolicitada autorizacao,
            JsonNode ocorrencia) {

        autorizacao.setProtocolo(
                textoOuNull(
                        ocorrencia.path("protocolo")));

        JsonNode idOcorrenciaNode = ocorrencia.path("idOcorrencia");

        if (!idOcorrenciaNode.isMissingNode()
                && !idOcorrenciaNode.isNull()
                && idOcorrenciaNode.canConvertToLong()) {

            autorizacao.setMosiaOcorrenciaId(
                    idOcorrenciaNode.asLong());
        }

        JsonNode assunto = ocorrencia.path("assunto");

        if (!assunto.isMissingNode()
                && !assunto.isNull()) {

            autorizacao.setIdTipoGuia(
                    textoOuNull(
                            assunto.path("id")));

            autorizacao.setTipoGuia(
                    textoOuNull(
                            assunto.path("descricao")));
        }
    }

    /**
     * A chave utilizada nas atualizações de status é a chave
     * única do solicitante da ocorrência.
     */
    private void preencherSolicitante(
            AutorizacaoSolicitada autorizacao,
            JsonNode ocorrencia) {

        JsonNode solicitante = ocorrencia.path("solicitante");

        if (solicitante.isMissingNode()
                || solicitante.isNull()) {

            log.warn(
                    "[AUTORIZACAO MAPPER] "
                            + "Solicitante ausente. protocolo={}",
                    autorizacao.getProtocolo());

            return;
        }

        String chaveSolicitante = somenteNumeros(
                textoOuNull(
                        solicitante.path("chaveUnica")));

        autorizacao.setChaveUnica(
                chaveSolicitante);

        log.info(
                "[AUTORIZACAO MAPPER] "
                        + "Chave do solicitante capturada. "
                        + "protocolo={} chave={}",
                autorizacao.getProtocolo(),
                mascararChave(chaveSolicitante));
    }

    /**
     * Os dados do beneficiário são utilizados apenas para informações
     * do contrato. A chave do beneficiário não deve ser gravada no
     * campo CHAVEUNICA utilizado para atualizar a ocorrência.
     */
    private void preencherBeneficiario(
            AutorizacaoSolicitada autorizacao,
            JsonNode ocorrencia) {

        JsonNode beneficiario = ocorrencia.path("beneficiario");

        if (beneficiario.isMissingNode()
                || beneficiario.isNull()) {

            return;
        }

        autorizacao.setNumeroContrato(
                textoOuNull(
                        beneficiario.path("numeroContrato")));
    }

    private void preencherStatus(
            AutorizacaoSolicitada autorizacao,
            JsonNode ocorrencia) {

        JsonNode statusNode = ocorrencia
                .path("atendimento")
                .path("status");

        if (statusNode.isMissingNode()
                || statusNode.isNull()) {

            return;
        }

        autorizacao.setStatus(
                textoOuNull(
                        statusNode.path("descricao")));

        autorizacao.setStatusId(
                textoOuNull(
                        statusNode.path("id")));
    }

    private void preencherFormulario(
            AutorizacaoSolicitada autorizacao,
            JsonNode ocorrencia) {

        JsonNode formularios = ocorrencia
                .path("solicitacao")
                .path("formularios");

        if (!formularios.isArray()
                || formularios.isEmpty()) {

            log.warn(
                    "[AUTORIZACAO MAPPER] "
                            + "Nenhum formulário encontrado. protocolo={}",
                    autorizacao.getProtocolo());

            return;
        }

        JsonNode primeiroFormulario = formularios.get(0);

        autorizacao.setLabel(
                textoOuNull(
                        primeiroFormulario.path("nome")));

        JsonNode itens = primeiroFormulario.path("itens");

        if (!itens.isArray()
                || itens.isEmpty()) {

            return;
        }

        JsonNode primeiroItem = itens.get(0);

        autorizacao.setNome(
                textoOuNull(
                        primeiroItem.path("nome-do-contato")));

        autorizacao.setTelefoneContato(
                textoOuNull(
                        primeiroItem.path("celular-do-contato")));

        String email = textoOuNull(
                primeiroItem.path("email"));

        if (email == null) {
            email = textoOuNull(
                    primeiroItem.path(
                            "email-do-contato"));
        }

        autorizacao.setEmailContato(email);

        autorizacao.setObservacao(
                textoOuNull(
                        primeiroItem.path("observacao")));
    }

    private void preencherArquivos(
            AutorizacaoSolicitada autorizacao,
            JsonNode ocorrencia) {

        List<AutorizacaoArquivo> arquivos = new ArrayList<>();

        JsonNode formularios = ocorrencia
                .path("solicitacao")
                .path("formularios");

        if (!formularios.isArray()) {
            autorizacao.setArquivos(arquivos);
            return;
        }

        for (JsonNode formulario : formularios) {

            JsonNode itens = formulario.path("itens");

            if (!itens.isArray()) {
                continue;
            }

            for (JsonNode item : itens) {

                JsonNode arquivosNode = item.path("arquivo");

                if (!arquivosNode.isArray()) {
                    continue;
                }

                for (JsonNode arquivoNode : arquivosNode) {

                    String nome = textoOuNull(
                            arquivoNode.path("name"));

                    String url = textoOuNull(
                            arquivoNode.path("url"));

                    if (url == null) {
                        continue;
                    }

                    AutorizacaoArquivo arquivo = new AutorizacaoArquivo();

                    arquivo.setNomeOriginal(nome);
                    arquivo.setUrlOriginal(url);
                    arquivo.setAutorizacao(
                            autorizacao);

                    arquivos.add(arquivo);
                }
            }
        }

        autorizacao.setArquivos(arquivos);
    }

    private String textoOuNull(
            JsonNode node) {

        if (node == null
                || node.isMissingNode()
                || node.isNull()) {

            return null;
        }

        String valor = node.asText();

        if (valor == null) {
            return null;
        }

        String normalizado = valor.trim();

        return normalizado.isEmpty()
                ? null
                : normalizado;
    }

    private String somenteNumeros(
            String valor) {

        if (valor == null) {
            return null;
        }

        String normalizado = valor.replaceAll("\\D", "");

        return normalizado.isBlank()
                ? null
                : normalizado;
    }

    private String mascararChave(
            String chave) {

        if (chave == null
                || chave.length() < 4) {

            return "***";
        }

        return "***"
                + chave.substring(
                        chave.length() - 4);
    }
}
