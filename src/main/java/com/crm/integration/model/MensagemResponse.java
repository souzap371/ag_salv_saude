package com.crm.integration.model;

public class MensagemResponse {
    private boolean success;
    private String message;
    private int status;
    private long timestamp;
    private Object data;

    public MensagemResponse(boolean success, String message, int status, long timestamp, Object data) {
        this.success = success;
        this.message = message;
        this.status = status;
        this.timestamp = timestamp;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Object getData() {
        return data;
    }
}
