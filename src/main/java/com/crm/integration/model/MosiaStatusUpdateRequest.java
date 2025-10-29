package com.crm.integration.model;


public class MosiaStatusUpdateRequest {
    private String protocolo;
    private String status;

    public MosiaStatusUpdateRequest() {}

    public MosiaStatusUpdateRequest(String protocolo, String status) {
        this.protocolo = protocolo;
        this.status = status;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
