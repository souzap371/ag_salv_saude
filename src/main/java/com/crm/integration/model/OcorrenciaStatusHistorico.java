package com.crm.integration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ocorrencia_status_historico")
public class OcorrenciaStatusHistorico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ocorrencia_id", nullable = false)
    @JsonIgnore
    private Ocorrencia ocorrencia;
    @Column(name = "status_id")
    private String statusId;
    @Column(name = "descricao", nullable = false)
    private String descricao;
    @Column(name = "origem", nullable = false, length = 30)
    private String origem;
    @Column(name = "atribuido_em", nullable = false)
    private OffsetDateTime atribuidoEm;

    @PrePersist
    void prePersist() {
        if (atribuidoEm == null) atribuidoEm = OffsetDateTime.now();
        if (origem == null || origem.isBlank()) origem = "CRM";
    }

    public Long getId() { return id; }
    public Ocorrencia getOcorrencia() { return ocorrencia; }
    public void setOcorrencia(Ocorrencia ocorrencia) { this.ocorrencia = ocorrencia; }
    public String getStatusId() { return statusId; }
    public void setStatusId(String statusId) { this.statusId = statusId; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public OffsetDateTime getAtribuidoEm() { return atribuidoEm; }
}
