package com.crm.integration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "ocorrencia_tag")
public class OcorrenciaTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ocorrencia_id", nullable = false)
    @JsonIgnore
    private Ocorrencia ocorrencia;
    @Column(nullable = false)
    private String chave;
    private String valor;

    public Long getId() { return id; }
    public Ocorrencia getOcorrencia() { return ocorrencia; }
    public void setOcorrencia(Ocorrencia ocorrencia) { this.ocorrencia = ocorrencia; }
    public String getChave() { return chave; }
    public void setChave(String chave) { this.chave = chave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
