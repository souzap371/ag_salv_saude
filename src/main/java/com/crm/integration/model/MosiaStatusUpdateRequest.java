package com.crm.integration.model;

public class MosiaStatusUpdateRequest {
    private Integer idNovoStatus;
    private String textoDesfecho;
    private Integer idMotivoEncerramento;
    private Integer idAtendente;
    private Boolean manterAtribuicao;
    private String chaveUnicaSolicitante;
    private String protocolo;
    private Long numeroGuia;



    // getters e setters
    public Integer getIdNovoStatus() { return idNovoStatus; }
    public void setIdNovoStatus(Integer idNovoStatus) { this.idNovoStatus = idNovoStatus; }

    public String getTextoDesfecho() { return textoDesfecho; }
    public void setTextoDesfecho(String textoDesfecho) { this.textoDesfecho = textoDesfecho; }

    public Integer getIdMotivoEncerramento() { return idMotivoEncerramento; }
    public void setIdMotivoEncerramento(Integer idMotivoEncerramento) { this.idMotivoEncerramento = idMotivoEncerramento; }

    public Integer getIdAtendente() { return idAtendente; }
    public void setIdAtendente(Integer idAtendente) { this.idAtendente = idAtendente; }

    public Boolean getManterAtribuicao() { return manterAtribuicao; }
    public void setManterAtribuicao(Boolean manterAtribuicao) { this.manterAtribuicao = manterAtribuicao; }

    public String getChaveUnicaSolicitante() { return chaveUnicaSolicitante; }
    public void setChaveUnicaSolicitante(String chaveUnicaSolicitante) { this.chaveUnicaSolicitante = chaveUnicaSolicitante; }

    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }
    public Long getNumeroGuia() { return numeroGuia; }
    public void setNumeroGuia(Long numeroGuia) { this.numeroGuia = numeroGuia; }
}
