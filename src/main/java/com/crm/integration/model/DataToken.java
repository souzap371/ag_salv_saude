package com.crm.integration.model;


public class DataToken {
    private String token;
    private long expireAt;

    public DataToken(String token, long expireAt) {
        this.token = token;
        this.expireAt = expireAt;
    }

    public String getToken() {
        return token;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }
}
