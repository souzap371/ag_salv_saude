package com.crm.integration.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Lob;

@Entity
@Table(name = "formularios")
public class Formulario {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_formularios")
    @SequenceGenerator(
            name = "seq_formularios",
            sequenceName = "SEQ_FORMULARIOS",
            allocationSize = 1)
    private Long id;

    private String idForm;

    private String nome;

    @ElementCollection
    @CollectionTable(name = "form_data", joinColumns = @JoinColumn(name = "formulario_id"))
    @MapKeyColumn(name = "campo")
    @Column(name = "valor")
    @JsonIgnore
    private Map<String, String> formData;

    @Lob
    @Column(name = "dados_json")
    @JsonIgnore
    private String dadosJson;

    @JsonProperty("formData")
    public JsonNode getFormDataJson() {
        if (dadosJson == null || dadosJson.isBlank()) return null;
        try {
            return JSON.readTree(dadosJson);
        } catch (Exception ex) {
            throw new IllegalStateException("JSON de formulário inválido no banco", ex);
        }
    }

    @JsonProperty("formData")
    public void setFormDataJson(JsonNode formDataJson) {
        this.dadosJson = formDataJson == null ? null : formDataJson.toString();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdForm() {
        return idForm;
    }

    public void setIdForm(String idForm) {
        this.idForm = idForm;
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Map<String, String> getFormData() {
        return formData;
    }

    public void setFormData(Map<String, String> formData) {
        this.formData = formData;
    }

    public String getDadosJson() { return dadosJson; }
    public void setDadosJson(String dadosJson) { this.dadosJson = dadosJson; }
    
}
