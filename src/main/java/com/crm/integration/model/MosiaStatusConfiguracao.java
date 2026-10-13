package com.crm.integration.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;

@Entity
@Table(name = "mosia_status_config", uniqueConstraints = @UniqueConstraint(
        name = "uk_mosia_status_tipo",
        columnNames = {"id_status_mosia", "id_tipo_ocorrencia_mosia"}))
public class MosiaStatusConfiguracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_status_mosia", nullable = false)
    private Integer idStatusMosia;

    @Column(name = "id_tipo_ocorrencia_mosia", nullable = false)
    private Integer idTipoOcorrenciaMosia;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false, length = 30)
    private String origem;

    @Column(name = "primeira_identificacao", nullable = false)
    private OffsetDateTime primeiraIdentificacao;

    @Column(name = "ultima_identificacao", nullable = false)
    private OffsetDateTime ultimaIdentificacao;

    @PrePersist
    void prePersist() {
        OffsetDateTime agora = OffsetDateTime.now();
        if (primeiraIdentificacao == null) primeiraIdentificacao = agora;
        if (ultimaIdentificacao == null) ultimaIdentificacao = agora;
        if (origem == null || origem.isBlank()) origem = "PAINEL";
    }

    public Long getId() { return id; }
    public Integer getIdStatusMosia() { return idStatusMosia; }
    public void setIdStatusMosia(Integer idStatusMosia) { this.idStatusMosia = idStatusMosia; }
    public Integer getIdTipoOcorrenciaMosia() { return idTipoOcorrenciaMosia; }
    public void setIdTipoOcorrenciaMosia(Integer idTipoOcorrenciaMosia) { this.idTipoOcorrenciaMosia = idTipoOcorrenciaMosia; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public OffsetDateTime getPrimeiraIdentificacao() { return primeiraIdentificacao; }
    public void setPrimeiraIdentificacao(OffsetDateTime valor) { this.primeiraIdentificacao = valor; }
    public OffsetDateTime getUltimaIdentificacao() { return ultimaIdentificacao; }
    public void setUltimaIdentificacao(OffsetDateTime valor) { this.ultimaIdentificacao = valor; }
}
