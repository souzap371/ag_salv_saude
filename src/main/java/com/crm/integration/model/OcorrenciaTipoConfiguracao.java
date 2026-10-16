package com.crm.integration.model;

import com.crm.integration.enums.TipoNegocioOcorrencia;
import jakarta.persistence.*;

@Entity
@Table(name = "ocorrencia_tipo_config")
public class OcorrenciaTipoConfiguracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_negocio", nullable = false, length = 60)
    private TipoNegocioOcorrencia tipoNegocio;

    @Column(name = "codigo_funcionalidade", length = 100)
    private String idFuncionalidade;

    @Column(name = "id_tipo_ocorrencia")
    private Integer idTipoOcorrencia;

    @Column(name = "id_assunto")
    private Integer idAssunto;

    @Column(nullable = false)
    private boolean ativo = true;

    public Long getId() { return id; }
    public TipoNegocioOcorrencia getTipoNegocio() { return tipoNegocio; }
    public void setTipoNegocio(TipoNegocioOcorrencia tipoNegocio) { this.tipoNegocio = tipoNegocio; }
    public String getIdFuncionalidade() { return idFuncionalidade; }
    public void setIdFuncionalidade(String idFuncionalidade) { this.idFuncionalidade = idFuncionalidade; }
    public Integer getIdTipoOcorrencia() { return idTipoOcorrencia; }
    public void setIdTipoOcorrencia(Integer idTipoOcorrencia) { this.idTipoOcorrencia = idTipoOcorrencia; }
    public Integer getIdAssunto() { return idAssunto; }
    public void setIdAssunto(Integer idAssunto) { this.idAssunto = idAssunto; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
