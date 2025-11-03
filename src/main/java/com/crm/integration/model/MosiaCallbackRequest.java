package com.crm.integration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MosiaCallbackRequest {

    private Ocorrencia ocorrencia;

    // Getters e setters
    public Ocorrencia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(Ocorrencia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ocorrencia {
        private String protocolo;
        private Solicitante solicitante;
        private Atendimento atendimento;

        public String getProtocolo() {
            return protocolo;
        }

        public void setProtocolo(String protocolo) {
            this.protocolo = protocolo;
        }

        public Solicitante getSolicitante() {
            return solicitante;
        }

        public void setSolicitante(Solicitante solicitante) {
            this.solicitante = solicitante;
        }

        public Atendimento getAtendimento() {
            return atendimento;
        }

        public void setAtendimento(Atendimento atendimento) {
            this.atendimento = atendimento;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Solicitante {
        private String chaveUnica;

        public String getChaveUnica() {
            return chaveUnica;
        }

        public void setChaveUnica(String chaveUnica) {
            this.chaveUnica = chaveUnica;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Atendimento {
        private Atendente atendente;
        private Status status;

        public Atendente getAtendente() {
            return atendente;
        }

        public void setAtendente(Atendente atendente) {
            this.atendente = atendente;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Atendente {
        private Integer id;
        private String nome;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private Object id;
        private String descricao;

        public String getId() {
            return id != null ? String.valueOf(id) : null;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }
    }

}
