package com.crm.integration.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class CallbackSolicitacaoDTO {

    @JsonProperty("protocolo")
    private String protocolo;

    @JsonProperty("id_operadora")
    private Integer idOperadora;

    @JsonProperty("mshash")
    private String mshash;

    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("matricula")
    private String matricula;

    @JsonProperty("matricula_titular")
    private String matriculaTitular;

    @JsonProperty("equipe_atendimento_cms")
    private String equipeAtendimentoCms;

    @JsonProperty("login")
    private String login;

    @JsonProperty("id_ms_usuario")
    private String idMsUsuario;

    @JsonProperty("tipo_guia")
    private String tipoGuia;

    @JsonProperty("id_tipo_guia")
    private String idTipoGuia;

    @JsonProperty("label")
    private String label; // título do formulário

    @JsonProperty("campos")
    private List<FormCampoDTO> campos;
}
