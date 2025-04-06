package com.crm.integration.model;


public class OcorrenciaResponse {
    private boolean status;
    private String message;
    private int statusCode;
    private long timestamp;
    private Ocorrencia data;



    public OcorrenciaResponse(boolean status, String message, int statusCode, long timestamp, Ocorrencia data) {
        this.status = status;
        this.message = message;
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.data = data;
    }


    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Ocorrencia getData() {
        return data;
    }

    public void setData(Ocorrencia data) {
        this.data = data;
    }

}
