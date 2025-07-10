package com.crm.integration.dto;

import com.crm.integration.model.Funcionalidade;
import com.crm.integration.model.Request;

public class MosiaRequestDTO {
    private String instancia_aplicacao;
    private Request request;
    private Funcionalidade funcionalidade;
    private OcorrenciaMosia ocorrencia;

    // Getters e Setters

    public String getInstancia_aplicacao() {
        return instancia_aplicacao;
    }

    public void setInstancia_aplicacao(String instancia_aplicacao) {
        this.instancia_aplicacao = instancia_aplicacao;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Funcionalidade getFuncionalidade() {
        return funcionalidade;
    }

    public void setFuncionalidade(Funcionalidade funcionalidade) {
        this.funcionalidade = funcionalidade;
    }

    public OcorrenciaMosia getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(OcorrenciaMosia ocorrencia) {
        this.ocorrencia = ocorrencia;
    }
}
