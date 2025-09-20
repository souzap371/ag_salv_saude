package com.crm.integration.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "autorizacao_arquivo")
public class AutorizacaoArquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeOriginal;
    private String urlOriginal; // URL assinada enviada no callback
    private String caminhoLocal; // onde salvamos (ex: /data/arquivos/...)
    private OffsetDateTime baixadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autorizacao_id")
    private AutorizacaoSolicitada autorizacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getUrlOriginal() {
        return urlOriginal;
    }

    public void setUrlOriginal(String urlOriginal) {
        this.urlOriginal = urlOriginal;
    }

    public String getCaminhoLocal() {
        return caminhoLocal;
    }

    public void setCaminhoLocal(String caminhoLocal) {
        this.caminhoLocal = caminhoLocal;
    }

    public OffsetDateTime getBaixadoEm() {
        return baixadoEm;
    }

    public void setBaixadoEm(OffsetDateTime baixadoEm) {
        this.baixadoEm = baixadoEm;
    }

    public AutorizacaoSolicitada getAutorizacao() {
        return autorizacao;
    }

    public void setAutorizacao(AutorizacaoSolicitada autorizacao) {
        this.autorizacao = autorizacao;
    }

}
