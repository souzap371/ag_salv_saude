package com.crm.integration.dto;

import java.util.List;

public class Solicitacao {
    private Template template;
    private List<FormularioDTO> formularios;

    // Getters e Setters

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public List<FormularioDTO> getFormularios() {
        return formularios;
    }

    public void setFormularios(List<FormularioDTO> formularios) {
        this.formularios = formularios;
    }
}
