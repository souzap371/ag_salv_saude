package com.crm.integration.service;

import com.crm.integration.enums.TipoNegocioOcorrencia;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaTipoConfiguracao;
import com.crm.integration.repository.OcorrenciaTipoConfiguracaoRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;

@Service
public class OcorrenciaClassificacaoService {
    private final OcorrenciaTipoConfiguracaoRepository repository;

    public OcorrenciaClassificacaoService(OcorrenciaTipoConfiguracaoRepository repository) {
        this.repository = repository;
    }

    public TipoNegocioOcorrencia classificar(Ocorrencia ocorrencia) {
        TipoNegocioOcorrencia tipo = repository.findByAtivoTrue().stream()
                .filter(config -> corresponde(config, ocorrencia))
                .max(Comparator.comparingInt(this::especificidade))
                .map(OcorrenciaTipoConfiguracao::getTipoNegocio)
                .orElse(TipoNegocioOcorrencia.OUTRA);
        ocorrencia.setTipoNegocio(tipo);
        return tipo;
    }

    private boolean corresponde(OcorrenciaTipoConfiguracao config, Ocorrencia ocorrencia) {
        return (config.getIdFuncionalidade() == null || config.getIdFuncionalidade().equals(ocorrencia.getIdFuncionalidade()))
                && (config.getIdTipoOcorrencia() == null || config.getIdTipoOcorrencia().equals(ocorrencia.getIdTipoOcorrencia()))
                && (config.getIdAssunto() == null || config.getIdAssunto().equals(ocorrencia.getIdAssunto()));
    }

    private int especificidade(OcorrenciaTipoConfiguracao config) {
        int pontos = 0;
        if (config.getIdFuncionalidade() != null) pontos++;
        if (config.getIdTipoOcorrencia() != null) pontos++;
        if (config.getIdAssunto() != null) pontos++;
        return pontos;
    }
}
