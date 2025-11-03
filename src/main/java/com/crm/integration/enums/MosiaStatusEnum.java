package com.crm.integration.enums;

public enum MosiaStatusEnum {

    ENVIADO(4112, "Enviado"),
    PROCESSADO_CONSULTE_LIBERACOES(4121, "Processado - Consulte Liberações");

    private final int codigo;
    private final String nome;

    MosiaStatusEnum(int codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public static String getNomePorCodigo(Integer codigo) {
        if (codigo == null) return null;
        for (MosiaStatusEnum status : values()) {
            if (status.getCodigo() == codigo) {
                return status.getNome();
            }
        }
        return "Desconhecido (" + codigo + ")";
    }
}
