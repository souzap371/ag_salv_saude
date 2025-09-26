package com.crm.integration.service;

import com.crm.integration.model.AgMensApp;
import com.crm.integration.model.MensagemRequest;
import com.crm.integration.repository.AgMensAppRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MensageriaService {

    private final AgMensAppRepository repository;

    public MensageriaService(AgMensAppRepository repository) {
        this.repository = repository;
    }

    public AgMensApp salvarMensagem(MensagemRequest request, Long idFuncionalidade, Long idRegistro) {
        AgMensApp msg = new AgMensApp();
        msg.setChaveUnica(UUID.randomUUID().toString()); // gera chave única
        msg.setMensagem(request.getMensagem());
        msg.setDtInclusao(LocalDateTime.now());
        msg.setProc("API"); // pode ser fixo ou vindo do request
        msg.setIdFuncionalidade(idFuncionalidade);
        msg.setIdRegistro(idRegistro);

        return repository.save(msg);
    }
}
