package com.crm.integration.dto;

public class MensagemRequest {
    private String chaveUnica;
    private String msg;

    // Getters e Setters
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
