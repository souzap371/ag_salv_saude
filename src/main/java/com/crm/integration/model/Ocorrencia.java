package com.crm.integration.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import com.crm.integration.enums.TipoNegocioOcorrencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "ocorrencias")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String protocolo;
    private int idTipoOcorrencia;
      @Column(name = "codigo_funcionalidade", length = 100)
      private String idFuncionalidade;
    private String numeroContrato;
    private Integer idAssunto;
    private boolean rascunho;
    // private String anotacao;
    private LocalDateTime dataRegistro;
    private LocalDateTime dataAtualizacao;

    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_negocio", nullable = false, length = 60)
    private TipoNegocioOcorrencia tipoNegocio = TipoNegocioOcorrencia.OUTRA;

    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Anotacao> anotacoes;

    @OneToOne(cascade = CascadeType.ALL)
    private Solicitante solicitante;

    @ManyToOne
    private Beneficiario beneficiario;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Formulario> formularios;

    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcorrenciaArquivo> arquivos = new ArrayList<>();

    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcorrenciaTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcorrenciaStatusHistorico> historicoStatus = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void vincularFilhos() {
        arquivos.forEach(arquivo -> arquivo.setOcorrencia(this));
        tags.forEach(tag -> tag.setOcorrencia(this));
        historicoStatus.forEach(historico -> historico.setOcorrencia(this));
    }

    public Long getId() {
        return id;
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

    public int getIdTipoOcorrencia() {
        return idTipoOcorrencia;
    }

    public void setIdTipoOcorrencia(int idTipoOcorrencia) {
        this.idTipoOcorrencia = idTipoOcorrencia;
    }

      public String getIdFuncionalidade() {
        return idFuncionalidade;
    }

      public void setIdFuncionalidade(String idFuncionalidade) {
        this.idFuncionalidade = idFuncionalidade;
    }

    public String getNumeroContrato() {
        return numeroContrato;
    }

    public void setNumeroContrato(String numeroContrato) {
        this.numeroContrato = numeroContrato;
    }

    public Integer getIdAssunto() {
        return idAssunto;
    }

    public void setIdAssunto(Integer idAssunto) {
        this.idAssunto = idAssunto;
    }

    public boolean getRascunho() {
        return rascunho;
    }

    public void setRascunho(boolean rascunho) {
        this.rascunho = rascunho;
    }

    // public String getAnotacao() {
    // return anotacao;
    // }

    // public void setAnotacao(String anotacao) {
    // this.anotacao = anotacao;
    // }

    public List<Anotacao> getAnotacoes() {
        return anotacoes;
    }

    public void setAnotacoes(List<Anotacao> anotacoes) {
        this.anotacoes = anotacoes;
    }

    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDateTime dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public TipoNegocioOcorrencia getTipoNegocio() { return tipoNegocio; }
    public void setTipoNegocio(TipoNegocioOcorrencia tipoNegocio) {
        this.tipoNegocio = tipoNegocio == null ? TipoNegocioOcorrencia.OUTRA : tipoNegocio;
    }

    public Solicitante getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(Solicitante solicitante) {
        this.solicitante = solicitante;
    }

    public Beneficiario getBeneficiario() {
        return beneficiario;
    }

    public void setBeneficiario(Beneficiario beneficiario) {
        this.beneficiario = beneficiario;
    }

    public List<Formulario> getFormularios() {
        return formularios;
    }

    public void setFormularios(List<Formulario> formularios) {
        this.formularios = formularios;
    }

    public List<OcorrenciaArquivo> getArquivos() { return arquivos; }
    public void setArquivos(List<OcorrenciaArquivo> arquivos) {
        this.arquivos = arquivos == null ? new ArrayList<>() : arquivos;
    }
    public List<OcorrenciaTag> getTags() { return tags; }
    public void setTags(List<OcorrenciaTag> tags) {
        this.tags = tags == null ? new ArrayList<>() : tags;
    }
    public List<OcorrenciaStatusHistorico> getHistoricoStatus() { return historicoStatus; }
    public void setHistoricoStatus(List<OcorrenciaStatusHistorico> historicoStatus) {
        this.historicoStatus = historicoStatus == null ? new ArrayList<>() : historicoStatus;
    }

}
