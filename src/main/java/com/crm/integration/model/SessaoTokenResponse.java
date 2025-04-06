package com.crm.integration.model;


public class SessaoTokenResponse {
    private boolean status;
    private SessaoTokenData data;
    private String message;
    private int statusCode;
    private long timestamp;

    public SessaoTokenResponse(boolean status, SessaoTokenData data, String message, int statusCode, long timestamp) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.statusCode = statusCode;
        this.timestamp = timestamp;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public SessaoTokenData getData() {
        return data;
    }

    public void setData(SessaoTokenData data) {
        this.data = data;
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
}
