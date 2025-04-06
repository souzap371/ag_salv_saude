package com.crm.integration.model;


public class MeetingResponse {
    private boolean status;
    private String message;
    private int statusCode;
    private long timestamp;
    private Object data;

    public MeetingResponse(boolean status, String message, int statusCode, long timestamp, Object data) {
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

