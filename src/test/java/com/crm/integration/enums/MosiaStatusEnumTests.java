package com.crm.integration.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MosiaStatusEnumTests {

    @Test
    void reconheceTodosOsStatusDeAutorizacoesDaSalv() {
        assertEquals(14, MosiaStatusEnum.statusAutorizacoesSalv().size());
        assertEquals("Solicitação Registrada - Ag. Análise", MosiaStatusEnum.getNomePorCodigo(5579));
        assertEquals("Em Análise da Solicitação Enviada", MosiaStatusEnum.getNomePorCodigo(5580));
        assertEquals("Solicitação Aprovada - Verifique Detalhes no Histórico", MosiaStatusEnum.getNomePorCodigo(5581));
        assertEquals("Pedido Cancelado Por Iniciativa do Solicitante/Beneficiário", MosiaStatusEnum.getNomePorCodigo(5582));
        assertEquals("Em Análise da Documentação Enviada", MosiaStatusEnum.getNomePorCodigo(5583));
        assertEquals("Em Análise Pela Equipe Técnica - Aguarde o Resultado", MosiaStatusEnum.getNomePorCodigo(5584));
        assertEquals("Recusado - Consulte o Motivo no Histórico da Solicitação", MosiaStatusEnum.getNomePorCodigo(5585));
        assertEquals("Recusado - Ausência de Justificativa Médica", MosiaStatusEnum.getNomePorCodigo(5586));
        assertEquals("Recusado - Documentação Enviada Inválida Para o Pedido", MosiaStatusEnum.getNomePorCodigo(5587));
        assertEquals("Recusado - Necessária Permanência de 24 Meses no Plano, Quando Aplicável", MosiaStatusEnum.getNomePorCodigo(5588));
        assertEquals("Em Auditoria", MosiaStatusEnum.getNomePorCodigo(5589));
        assertEquals("Análise Auditoria - Favorável", MosiaStatusEnum.getNomePorCodigo(5590));
        assertEquals("Análise Auditoria - Desfavorável", MosiaStatusEnum.getNomePorCodigo(5591));
        assertEquals("Em Atendimento", MosiaStatusEnum.getNomePorCodigo(5592));
    }

    @Test
    void converteDescricaoEmCodigoIgnorandoAcentosECaixa() {
        assertEquals(5586, MosiaStatusEnum.getCodigoPorNome("recusado - ausencia de justificativa medica"));
        assertEquals(5581, MosiaStatusEnum.getCodigoPorNome("SOLICITAÇÃO APROVADA - VERIFIQUE DETALHES NO HISTÓRICO"));
        assertNull(MosiaStatusEnum.getCodigoPorNome("Status inexistente"));
    }
}
