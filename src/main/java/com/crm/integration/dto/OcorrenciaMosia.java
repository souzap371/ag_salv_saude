package com.crm.integration.dto;

import java.util.List;

public class OcorrenciaMosia {
    private String protocolo;
    private String dataRegistro;
    private String dataAtualizacao;
    private Tipo tipoOcorrencia;
    private Tipo assunto;
    private Pessoa solicitante;
    private Pessoa beneficiario;
    private Solicitacao solicitacao;

    // Getters e Setters

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(String dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public String getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(String dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public Tipo getTipoOcorrencia() {
        return tipoOcorrencia;
    }

    public void setTipoOcorrencia(Tipo tipoOcorrencia) {
        this.tipoOcorrencia = tipoOcorrencia;
    }

    public Tipo getAssunto() {
        return assunto;
    }

    public void setAssunto(Tipo assunto) {
        this.assunto = assunto;
    }

    public Pessoa getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Pessoa solicitante) {
        this.solicitante = solicitante;
    }

    public Pessoa getBeneficiario() {
        return beneficiario;
    }

    public void setBeneficiario(Pessoa beneficiario) {
        this.beneficiario = beneficiario;
    }

    public Solicitacao getSolicitacao() {
        return solicitacao;
    }

    public void setSolicitacao(Solicitacao solicitacao) {
        this.solicitacao = solicitacao;
    }
}
