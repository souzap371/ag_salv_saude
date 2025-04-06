package com.crm.integration.model;


import java.util.List;

public class UltimosProtocolosResponse {
    private boolean status;
    private String message;
    private int statusCode;
    private long timestamp;
    private List<String> data;

    public UltimosProtocolosResponse(boolean status, String message, int statusCode, long timestamp, List<String> data) {
        this.status = status;
        this.message = message;
        this.statusCode = statusCode;
        this.timestamp = timestamp;
        this.data = data;
    }

    // Getters e Setters
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

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
