package com.crm.integration.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AG_MENSAPP")
public class AgMensApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CHAVE_UNICA")
    private String chaveUnica;

    @Column(name = "MENSAGEM", nullable = false)
    private String mensagem;

    @Column(name = "DTINCLUSAO")
    private LocalDateTime dtInclusao;

    @Column(name = "DTENVIO")
    private LocalDateTime dtEnvio;

    @Column(name = "PROC")
    private String proc;

    @Column(name = "IDFUNCIONALIDADE")
    private String idFuncionalidade;

    @Column(name = "IDREGISTRO")
    private String idRegistro;

    @Column(name = "NNUMEGUIA")
    private Long numeroGuia;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChaveUnica() {
        return chaveUnica;
    }

    public void setChaveUnica(String chaveUnica) {
        this.chaveUnica = chaveUnica;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getDtInclusao() {
        return dtInclusao;
    }

    public void setDtInclusao(LocalDateTime dtInclusao) {
        this.dtInclusao = dtInclusao;
    }

    public LocalDateTime getDtEnvio() {
        return dtEnvio;
    }

    public void setDtEnvio(LocalDateTime dtEnvio) {
        this.dtEnvio = dtEnvio;
    }

    public String getProc() {
        return proc;
    }

    public void setProc(String proc) {
        this.proc = proc;
    }

    public String getIdFuncionalidade() {
        return idFuncionalidade;
    }

    public void setIdFuncionalidade(String idFuncionalidade) {
        this.idFuncionalidade = idFuncionalidade;
    }

    public String getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(String idRegistro) {
        this.idRegistro = idRegistro;
    }

    public Long getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(Long numeroGuia) { this.numeroGuia = numeroGuia; }
}
