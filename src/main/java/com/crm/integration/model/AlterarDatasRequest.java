package com.crm.integration.model;


public class AlterarDatasRequest {
    // Timestamp em milissegundos para dataRegistro e dataAtualizacao
    private Long dataRegistro;
    private Long dataAtualizacao;

    public Long getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(Long dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public Long getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(Long dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}

