package com.crm.integration.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

// @Entity
// @Table(name = "autorizacao_solicitada", indexes = {
//         @Index(name = "idx_autorizacao_protocolo", columnList = "protocolo", unique = true)
// })


@Entity
@Table(name = "autorizacao_solicitada" /* sem unique index por protocolo */)


public class AutorizacaoSolicitada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(nullable = false, unique = true)
    @Column(nullable = false) // removido unique = true
    private String protocolo;
    private String nome;
    private String tipoGuia;
    private String idTipoGuia;
    private String label; // título do formulário

    private String telefoneContato;
    private String emailContato;
    private String observacao;

    private String status; // controle local (PENDENTE/APROVADO/REJEITADO)

     @Column(name = "STATUS_ID")
    private String statusId;

    

    @Lob
    private String rawCallbackJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "sincronizado_mosia")
private String sincronizadoMosia = "S"; // padrão: já sincronizado

@Column(name = "ultima_sincronizacao")
private OffsetDateTime ultimaSincronizacao;


    // @Column(name = "CHAVEUNICA", unique = true)
    // private String chaveUnica;
    @Column(name = "CHAVEUNICA") // removido unique = true
    private String chaveUnica;

    @Column(name = "NUMEROCONTRATO")
    private String numeroContrato;

    // RELAÇÃO COM AUTORIZACAOARQUIVO
    @OneToMany(mappedBy = "autorizacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AutorizacaoArquivo> arquivos = new ArrayList<>();

    // ======= MÉTODOS DE CICLO DE VIDA =======
    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getStatusId() {
        return statusId;
    }

     public void setStatusId(String statusId) {
         this.statusId = statusId;
     }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getChaveUnica() {
        return chaveUnica;
    }

    public void setChaveUnica(String chaveUnica) {
        this.chaveUnica = chaveUnica;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipoGuia() {
        return tipoGuia;
    }

    public void setTipoGuia(String tipoGuia) {
        this.tipoGuia = tipoGuia;
    }

    public String getIdTipoGuia() {
        return idTipoGuia;
    }

    public void setIdTipoGuia(String idTipoGuia) {
        this.idTipoGuia = idTipoGuia;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRawCallbackJson() {
        return rawCallbackJson;
    }

    public void setRawCallbackJson(String rawCallbackJson) {
        this.rawCallbackJson = rawCallbackJson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSincronizadoMosia() {
    return sincronizadoMosia;
}

public void setSincronizadoMosia(String sincronizadoMosia) {
    this.sincronizadoMosia = sincronizadoMosia;
}

public OffsetDateTime getUltimaSincronizacao() {
    return ultimaSincronizacao;
}

public void setUltimaSincronizacao(OffsetDateTime ultimaSincronizacao) {
    this.ultimaSincronizacao = ultimaSincronizacao;
}


    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<AutorizacaoArquivo> getArquivos() {
        return arquivos;
    }

    public void setArquivos(List<AutorizacaoArquivo> arquivos) {
        this.arquivos = arquivos;
    }

    public String getTelefoneContato() {
        return telefoneContato;
    }

    public String getEmailContato() {
        return emailContato;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setTelefoneContato(String telefoneContato) {
        this.telefoneContato = telefoneContato;
    }

    public void setEmailContato(String emailContato) {
        this.emailContato = emailContato;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}


// package com.crm.integration.model;

// import jakarta.persistence.*;
// import java.time.OffsetDateTime;
// import java.util.ArrayList;
// import java.util.List;

// @Entity
// @Table(name = "autorizacao_solicitada")
// public class AutorizacaoSolicitada {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false) // removido unique = true
//     private String protocolo;

//     private String nome;
//     private String tipoGuia;
//     private String idTipoGuia;
//     private String label; // título do formulário

//     private String telefoneContato;
//     private String emailContato;
//     private String observacao;

//     /** 
//      * Nome do status (ex: "Processado - Consulte Liberações")
//      */
//     private String status;

//     /**
//      * ID real do status recebido do MOSIA (ex: "4112")
//      */
//     @Column(name = "STATUS_ID")
//     private String statusId;

//     @Lob
//     private String rawCallbackJson;

//     @Column(name = "created_at", nullable = false, updatable = false)
//     private OffsetDateTime createdAt;

//     @Column(name = "updated_at", nullable = false)
//     private OffsetDateTime updatedAt;

//     @Column(name = "sincronizado_mosia")
//     private String sincronizadoMosia = "S"; // padrão: já sincronizado

//     @Column(name = "ultima_sincronizacao")
//     private OffsetDateTime ultimaSincronizacao;

//     @Column(name = "CHAVEUNICA") // removido unique = true
//     private String chaveUnica;

//     @Column(name = "NUMEROCONTRATO")
//     private String numeroContrato;

//     // RELAÇÃO COM AUTORIZACAOARQUIVO
//     @OneToMany(mappedBy = "autorizacao", cascade = CascadeType.ALL, orphanRemoval = true)
//     private List<AutorizacaoArquivo> arquivos = new ArrayList<>();

//     // ======= MÉTODOS DE CICLO DE VIDA =======
//     @PrePersist
//     public void prePersist() {
//         OffsetDateTime now = OffsetDateTime.now();
//         this.createdAt = now;
//         this.updatedAt = now;
//     }

//     @PreUpdate
//     public void preUpdate() {
//         this.updatedAt = OffsetDateTime.now();
//     }

//     // ======= GETTERS E SETTERS =======

//     public Long getId() {
//         return id;
//     }

//     public void setId(Long id) {
//         this.id = id;
//     }

//     public String getProtocolo() {
//         return protocolo;
//     }

//     public void setProtocolo(String protocolo) {
//         this.protocolo = protocolo;
//     }

//     public String getChaveUnica() {
//         return chaveUnica;
//     }

//     public void setChaveUnica(String chaveUnica) {
//         this.chaveUnica = chaveUnica;
//     }

//     public String getNumeroContrato() {
//         return numeroContrato;
//     }

//     public void setNumeroContrato(String numeroContrato) {
//         this.numeroContrato = numeroContrato;
//     }

//     public String getNome() {
//         return nome;
//     }

//     public void setNome(String nome) {
//         this.nome = nome;
//     }

//     public String getTipoGuia() {
//         return tipoGuia;
//     }

//     public void setTipoGuia(String tipoGuia) {
//         this.tipoGuia = tipoGuia;
//     }

//     public String getIdTipoGuia() {
//         return idTipoGuia;
//     }

//     public void setIdTipoGuia(String idTipoGuia) {
//         this.idTipoGuia = idTipoGuia;
//     }

//     public String getLabel() {
//         return label;
//     }

//     public void setLabel(String label) {
//         this.label = label;
//     }

//     public String getStatus() {
//         return status;
//     }

//     public void setStatus(String status) {
//         this.status = status;
//     }

//     public String getStatusId() {
//         return statusId;
//     }

//     public void setStatusId(String statusId) {
//         this.statusId = statusId;
//     }

//     public String getRawCallbackJson() {
//         return rawCallbackJson;
//     }

//     public void setRawCallbackJson(String rawCallbackJson) {
//         this.rawCallbackJson = rawCallbackJson;
//     }

//     public OffsetDateTime getCreatedAt() {
//         return createdAt;
//     }

//     public void setCreatedAt(OffsetDateTime createdAt) {
//         this.createdAt = createdAt;
//     }

//     public OffsetDateTime getUpdatedAt() {
//         return updatedAt;
//     }

//     public void setUpdatedAt(OffsetDateTime updatedAt) {
//         this.updatedAt = updatedAt;
//     }

//     public String getSincronizadoMosia() {
//         return sincronizadoMosia;
//     }

//     public void setSincronizadoMosia(String sincronizadoMosia) {
//         this.sincronizadoMosia = sincronizadoMosia;
//     }

//     public OffsetDateTime getUltimaSincronizacao() {
//         return ultimaSincronizacao;
//     }

//     public void setUltimaSincronizacao(OffsetDateTime ultimaSincronizacao) {
//         this.ultimaSincronizacao = ultimaSincronizacao;
//     }

//     public List<AutorizacaoArquivo> getArquivos() {
//         return arquivos;
//     }

//     public void setArquivos(List<AutorizacaoArquivo> arquivos) {
//         this.arquivos = arquivos;
//     }

//     public String getTelefoneContato() {
//         return telefoneContato;
//     }

//     public void setTelefoneContato(String telefoneContato) {
//         this.telefoneContato = telefoneContato;
//     }

//     public String getEmailContato() {
//         return emailContato;
//     }

//     public void setEmailContato(String emailContato) {
//         this.emailContato = emailContato;
//     }

//     public String getObservacao() {
//         return observacao;
//     }

//     public void setObservacao(String observacao) {
//         this.observacao = observacao;
//     }
// }
