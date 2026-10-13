package com.crm.integration.enums;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum MosiaStatusEnum {

    ENVIADO(4112, "Enviado"),
    PROCESSADO_CONSULTE_LIBERACOES(4121, "Processado - Consulte Liberações"),

    SOLICITACAO_REGISTRADA_AG_ANALISE(5579, "Solicitação Registrada - Ag. Análise"),
    EM_ANALISE_DA_SOLICITACAO_ENVIADA(5580, "Em Análise da Solicitação Enviada"),
    SOLICITACAO_APROVADA(5581, "Solicitação Aprovada - Verifique Detalhes no Histórico"),
    PEDIDO_CANCELADO_PELO_SOLICITANTE(5582, "Pedido Cancelado Por Iniciativa do Solicitante/Beneficiário"),
    EM_ANALISE_DA_DOCUMENTACAO_ENVIADA(5583, "Em Análise da Documentação Enviada"),
    EM_ANALISE_PELA_EQUIPE_TECNICA(5584, "Em Análise Pela Equipe Técnica - Aguarde o Resultado"),
    RECUSADO_CONSULTE_HISTORICO(5585, "Recusado - Consulte o Motivo no Histórico da Solicitação"),
    RECUSADO_AUSENCIA_JUSTIFICATIVA_MEDICA(5586, "Recusado - Ausência de Justificativa Médica"),
    RECUSADO_DOCUMENTACAO_INVALIDA(5587, "Recusado - Documentação Enviada Inválida Para o Pedido"),
    RECUSADO_PERMANENCIA_24_MESES(5588, "Recusado - Necessária Permanência de 24 Meses no Plano, Quando Aplicável"),
    EM_AUDITORIA(5589, "Em Auditoria"),
    ANALISE_AUDITORIA_FAVORAVEL(5590, "Análise Auditoria - Favorável"),
    ANALISE_AUDITORIA_DESFAVORAVEL(5591, "Análise Auditoria - Desfavorável"),
    EM_ATENDIMENTO(5592, "Em Atendimento");

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

    public static Integer getCodigoPorNome(String nome) {
        if (nome == null || nome.isBlank()) return null;
        String nomeNormalizado = normalizar(nome);
        for (MosiaStatusEnum status : values()) {
            if (normalizar(status.getNome()).equals(nomeNormalizado)) {
                return status.getCodigo();
            }
        }
        return null;
    }

    public static List<MosiaStatusEnum> statusAutorizacoesSalv() {
        return Arrays.stream(values())
                .filter(status -> status.codigo >= 5579 && status.codigo <= 5592)
                .toList();
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }
}
