package com.crm.integration.dto;

import java.util.List;
import java.util.Map;

public class FormularioDTO {
    private String nome;
    private String idForm;
    private List<Map<String, String>> itens;

    // Getters e Setters

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getIdForm() {
        return idForm;
    }

    public void setIdForm(String idForm) {
        this.idForm = idForm;
    }

    public List<Map<String, String>> getItens() {
        return itens;
    }

    public void setItens(List<Map<String, String>> itens) {
        this.itens = itens;
    }
}
