package com.crm.integration.model;

public class Request {
    private String nome;
    private String email;
    private String origem;

    public Request() {
    }

    public Request(String nome, String email, String origem) {
        this.nome = nome;
        this.email = email;
        this.origem = origem;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }
}
