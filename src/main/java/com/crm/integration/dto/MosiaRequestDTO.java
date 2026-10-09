package com.crm.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MosiaRequestDTO {
    private String instancia_aplicacao;
    private Request request;
    private Funcionalidade funcionalidade;
    private OcorrenciaMosia ocorrencia;

    public String getInstancia_aplicacao() { return instancia_aplicacao; }
    public void setInstancia_aplicacao(String instancia_aplicacao) { this.instancia_aplicacao = instancia_aplicacao; }
    public Request getRequest() { return request; }
    public void setRequest(Request request) { this.request = request; }
    public Funcionalidade getFuncionalidade() { return funcionalidade; }
    public void setFuncionalidade(Funcionalidade funcionalidade) { this.funcionalidade = funcionalidade; }
    public OcorrenciaMosia getOcorrencia() { return ocorrencia; }
    public void setOcorrencia(OcorrenciaMosia ocorrencia) { this.ocorrencia = ocorrencia; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OcorrenciaMosia {
        private String protocolo;
        private String dataRegistro;
        private String dataAtualizacao;
        private Referencia tipoOcorrencia;
        private Referencia assunto;
        private Pessoa solicitante;
        private Pessoa beneficiario;
        private Solicitacao solicitacao;
        private Atendimento atendimento;

        public String getProtocolo() { return protocolo; }
        public void setProtocolo(String protocolo) { this.protocolo = protocolo; }
        public String getDataRegistro() { return dataRegistro; }
        public void setDataRegistro(String dataRegistro) { this.dataRegistro = dataRegistro; }
        public String getDataAtualizacao() { return dataAtualizacao; }
        public void setDataAtualizacao(String dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
        public Referencia getTipoOcorrencia() { return tipoOcorrencia; }
        public void setTipoOcorrencia(Referencia tipoOcorrencia) { this.tipoOcorrencia = tipoOcorrencia; }
        public Referencia getAssunto() { return assunto; }
        public void setAssunto(Referencia assunto) { this.assunto = assunto; }
        public Pessoa getSolicitante() { return solicitante; }
        public void setSolicitante(Pessoa solicitante) { this.solicitante = solicitante; }
        public Pessoa getBeneficiario() { return beneficiario; }
        public void setBeneficiario(Pessoa beneficiario) { this.beneficiario = beneficiario; }
        public Solicitacao getSolicitacao() { return solicitacao; }
        public void setSolicitacao(Solicitacao solicitacao) { this.solicitacao = solicitacao; }
        public Atendimento getAtendimento() { return atendimento; }
        public void setAtendimento(Atendimento atendimento) { this.atendimento = atendimento; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Atendimento {
            private Status status;
            public Status getStatus() { return status; }
            public void setStatus(Status status) { this.status = status; }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Status {
                private String id;
                private String descricao;
                public Status() {}
                public Status(String descricao) { this.descricao = descricao; }
                public String getId() { return id; }
                public void setId(String id) { this.id = id; }
                public String getDescricao() { return descricao; }
                public void setDescricao(String descricao) { this.descricao = descricao; }
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Referencia {
        private Integer id;
        private String descricao;
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pessoa {
        private String nome;
        private String chaveUnica;
        private String numeroContrato;
        private String email;
        private String telefone;
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getChaveUnica() { return chaveUnica; }
        public void setChaveUnica(String chaveUnica) { this.chaveUnica = chaveUnica; }
        public String getNumeroContrato() { return numeroContrato; }
        public void setNumeroContrato(String numeroContrato) { this.numeroContrato = numeroContrato; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTelefone() { return telefone; }
        public void setTelefone(String telefone) { this.telefone = telefone; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Solicitacao {
        private List<FormularioPayload> formularios;
        public List<FormularioPayload> getFormularios() { return formularios; }
        public void setFormularios(List<FormularioPayload> formularios) { this.formularios = formularios; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FormularioPayload {
        private String nome;
        private String idForm;
        private JsonNode itens;
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getIdForm() { return idForm; }
        public void setIdForm(String idForm) { this.idForm = idForm; }
        public JsonNode getItens() { return itens; }
        public void setItens(JsonNode itens) { this.itens = itens; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        private String id;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Funcionalidade {
        private String id;
        private String descricao;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
    }
}
