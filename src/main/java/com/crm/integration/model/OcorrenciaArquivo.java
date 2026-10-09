package com.crm.integration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ocorrencia_arquivo")
public class OcorrenciaArquivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ocorrencia_id", nullable = false)
    @JsonIgnore
    private Ocorrencia ocorrencia;

    @Column(name = "nome_original", nullable = false)
    private String nomeOriginal;
    @Column(name = "tipo_mime", length = 150)
    private String tipoMime;
    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;
    @Column(name = "campo_origem")
    private String campoOrigem;
    @Column(name = "hash_sha256", length = 64)
    private String hashSha256;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "conteudo", nullable = false)
    private byte[] conteudo;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) criadoEm = OffsetDateTime.now();
        if (conteudo != null) tamanhoBytes = (long) conteudo.length;
    }

    public Long getId() { return id; }
    public Ocorrencia getOcorrencia() { return ocorrencia; }
    public void setOcorrencia(Ocorrencia ocorrencia) { this.ocorrencia = ocorrencia; }
    public String getNomeOriginal() { return nomeOriginal; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }
    public String getTipoMime() { return tipoMime; }
    public void setTipoMime(String tipoMime) { this.tipoMime = tipoMime; }
    public Long getTamanhoBytes() { return tamanhoBytes; }
    public String getCampoOrigem() { return campoOrigem; }
    public void setCampoOrigem(String campoOrigem) { this.campoOrigem = campoOrigem; }
    public String getHashSha256() { return hashSha256; }
    public void setHashSha256(String hashSha256) { this.hashSha256 = hashSha256; }
    public byte[] getConteudo() { return conteudo; }
    public void setConteudo(byte[] conteudo) { this.conteudo = conteudo; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}
