package com.crm.integration.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ocorrencia_tipo_config")
public class OcorrenciaTipoConfiguracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_negocio", nullable = false, length = 200)
    private String tipoNegocio;

    @Column(name = "codigo_funcionalidade", length = 100)
    private String idFuncionalidade;

    @Column(name = "id_tipo_ocorrencia")
    private Integer idTipoOcorrencia;

    @Column(name = "id_assunto")
    private Integer idAssunto;

    @Column(nullable = false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public String getTipoNegocio() { return tipoNegocio; }
    public void setTipoNegocio(String tipoNegocio) { this.tipoNegocio = tipoNegocio; }
    public String getIdFuncionalidade() { return idFuncionalidade; }
    public void setIdFuncionalidade(String idFuncionalidade) { this.idFuncionalidade = idFuncionalidade; }
    public Integer getIdTipoOcorrencia() { return idTipoOcorrencia; }
    public void setIdTipoOcorrencia(Integer idTipoOcorrencia) { this.idTipoOcorrencia = idTipoOcorrencia; }
    public Integer getIdAssunto() { return idAssunto; }
    public void setIdAssunto(Integer idAssunto) { this.idAssunto = idAssunto; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
