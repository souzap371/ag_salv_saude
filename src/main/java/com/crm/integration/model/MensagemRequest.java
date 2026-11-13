package com.crm.integration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mensagem enviada ao beneficiário pelo Mosia")
public class MensagemRequest {
    @Schema(description = "Chave única do beneficiário", example = "82459010249", requiredMode = Schema.RequiredMode.REQUIRED)
    private String chaveUnica; // equivalente ao CPF no Python

    @Schema(description = "Texto da notificação", example = "Sua solicitação foi atualizada.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String msg;

    @Schema(description = "Identificador opcional da funcionalidade do aplicativo que será aberta ao tocar no push", example = "31", nullable = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String idFuncionalidade;

    public String getChaveUnica() {
        return chaveUnica;
    }

    public void setChaveUnica(String chaveUnica) {
        this.chaveUnica = chaveUnica;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getIdFuncionalidade() {
        return idFuncionalidade;
    }

    public void setIdFuncionalidade(String idFuncionalidade) {
        this.idFuncionalidade = idFuncionalidade;
    }
}
