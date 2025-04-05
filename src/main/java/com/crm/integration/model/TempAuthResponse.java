package com.crm.integration.model;


public class TempAuthResponse {
    private boolean status;
    private DataToken data;
    private String message;
    private int statusCode;
    private long timestamp;

    public TempAuthResponse(boolean status, DataToken data, String message, int statusCode, long timestamp) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.statusCode = statusCode;
        this.timestamp = timestamp;
    }

    public boolean isStatus() {
        return status;
    }

    public DataToken getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setData(DataToken data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
