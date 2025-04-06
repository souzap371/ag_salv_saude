package com.crm.integration.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Um identificador único para o meeting (pode ser gerado ou recebido da API)
    @Column(unique = true)
    private String meetingId;

    private String assunto;
    private String descricao;

    // Status do meeting (por exemplo: "criado", "cancelado")
    private String status;

    // Campo para representar a fila atual (para atualização de fila)
    private String fila;

    // Status da consulta (por exemplo: "agendada", "cancelada", "encerrada")
    private String consultaStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFila() {
        return fila;
    }

    public void setFila(String fila) {
        this.fila = fila;
    }

    public String getConsultaStatus() {
        return consultaStatus;
    }

    public void setConsultaStatus(String consultaStatus) {
        this.consultaStatus = consultaStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

