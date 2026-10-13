package com.crm.integration.config;

import com.crm.integration.service.MosiaStatusConfiguracaoService;
import com.crm.integration.service.OcorrenciaTipoConfiguracaoService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MosiaStatusCatalogoInitializer implements ApplicationRunner {
    private final MosiaStatusConfiguracaoService service;
    private final OcorrenciaTipoConfiguracaoService tipoService;

    public MosiaStatusCatalogoInitializer(
            MosiaStatusConfiguracaoService service,
            OcorrenciaTipoConfiguracaoService tipoService) {
        this.service = service;
        this.tipoService = tipoService;
    }

    @Override
    public void run(ApplicationArguments args) {
        tipoService.inicializarTipoAutorizacoesSalv();
        service.inicializarCatalogoAutorizacoes();
    }
}
