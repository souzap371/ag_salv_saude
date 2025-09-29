package com.crm.integration.model;

public class MensagemRequest {
    private String chaveUnica; // equivalente ao CPF no Python
    private String msg;

    public String getChaveUnica() {
        return chaveUnica;
    }

    public void setChaveUnica(String chaveUnica) {
        this.chaveUnica = chaveUnica;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
