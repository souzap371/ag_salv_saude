package com.crm.integration.service;

import com.crm.integration.dto.MosiaStatusCatalogoResponse;
import com.crm.integration.dto.MosiaStatusConfigRequest;
import com.crm.integration.enums.MosiaStatusEnum;
import com.crm.integration.model.MosiaStatusConfiguracao;
import com.crm.integration.repository.MosiaStatusConfiguracaoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MosiaStatusConfiguracaoService {

    private static final int TIPO_AUTORIZACOES_SALV = 1895;
    private final MosiaStatusConfiguracaoRepository repository;

    public MosiaStatusConfiguracaoService(MosiaStatusConfiguracaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MosiaStatusCatalogoResponse> listar(Integer idTipoOcorrencia) {
        List<MosiaStatusConfiguracao> registros = idTipoOcorrencia == null
                ? repository.findAll()
                : repository.findByIdTipoOcorrenciaMosiaOrderByIdStatusMosia(idTipoOcorrencia);
        return registros.stream()
                .sorted((a, b) -> Integer.compare(a.getIdStatusMosia(), b.getIdStatusMosia()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MosiaStatusCatalogoResponse criar(MosiaStatusConfigRequest request) {
        validar(request);
        if (repository.findByIdStatusMosiaAndIdTipoOcorrenciaMosia(
                request.idStatusMosia(), request.idTipoOcorrenciaMosia()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este status já está cadastrado para o tipo informado.");
        }
        MosiaStatusConfiguracao entity = new MosiaStatusConfiguracao();
        copiar(request, entity);
        entity.setOrigem("PAINEL");
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public MosiaStatusCatalogoResponse atualizar(Long id, MosiaStatusConfigRequest request) {
        validar(request);
        MosiaStatusConfiguracao entity = buscar(id);
        Optional<MosiaStatusConfiguracao> duplicado = repository
                .findByIdStatusMosiaAndIdTipoOcorrenciaMosia(request.idStatusMosia(), request.idTipoOcorrenciaMosia());
        if (duplicado.isPresent() && !duplicado.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este status já está cadastrado para o tipo informado.");
        }
        copiar(request, entity);
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscar(id));
        repository.flush();
    }

    @Transactional
    public void registrarObservado(Integer idTipoOcorrencia, String idStatus, String descricao) {
        if (idTipoOcorrencia == null || idTipoOcorrencia <= 0 || idStatus == null || idStatus.isBlank()
                || descricao == null || descricao.isBlank()) return;
        try {
            registrarObservado(idTipoOcorrencia, Integer.valueOf(idStatus.trim()), descricao);
        } catch (NumberFormatException ignored) {
            // Payload inválido não deve impedir o processamento da ocorrência.
        }
    }

    @Transactional
    public void registrarObservado(Integer idTipoOcorrencia, Integer idStatus, String descricao) {
        if (idTipoOcorrencia == null || idTipoOcorrencia <= 0 || idStatus == null || idStatus <= 0
                || descricao == null || descricao.isBlank()) return;
        OffsetDateTime agora = OffsetDateTime.now();
        MosiaStatusConfiguracao entity = repository
                .findByIdStatusMosiaAndIdTipoOcorrenciaMosia(idStatus, idTipoOcorrencia)
                .orElseGet(() -> {
                    MosiaStatusConfiguracao novo = new MosiaStatusConfiguracao();
                    novo.setIdStatusMosia(idStatus);
                    novo.setIdTipoOcorrenciaMosia(idTipoOcorrencia);
                    novo.setPrimeiraIdentificacao(agora);
                    novo.setAtivo(true);
                    novo.setOrigem("MOSIA");
                    return novo;
                });
        entity.setDescricao(descricao.trim());
        entity.setUltimaIdentificacao(agora);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Integer buscarCodigoPorDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) return null;
        return repository.findAll().stream()
                .filter(MosiaStatusConfiguracao::isAtivo)
                .filter(status -> status.getDescricao().equalsIgnoreCase(descricao.trim()))
                .map(MosiaStatusConfiguracao::getIdStatusMosia)
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void inicializarCatalogoAutorizacoes() {
        for (MosiaStatusEnum status : MosiaStatusEnum.statusAutorizacoesSalv()) {
            if (repository.findByIdStatusMosiaAndIdTipoOcorrenciaMosia(
                    status.getCodigo(), TIPO_AUTORIZACOES_SALV).isEmpty()) {
                MosiaStatusConfiguracao entity = new MosiaStatusConfiguracao();
                entity.setIdStatusMosia(status.getCodigo());
                entity.setIdTipoOcorrenciaMosia(TIPO_AUTORIZACOES_SALV);
                entity.setDescricao(status.getNome());
                entity.setAtivo(true);
                entity.setOrigem("SISTEMA");
                repository.save(entity);
            }
        }
    }

    private MosiaStatusConfiguracao buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status não encontrado."));
    }

    private void validar(MosiaStatusConfigRequest request) {
        if (request == null || request.idStatusMosia() == null || request.idStatusMosia() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do status Mosia deve ser maior que zero.");
        if (request.idTipoOcorrenciaMosia() == null || request.idTipoOcorrenciaMosia() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O ID do tipo de ocorrência deve ser maior que zero.");
        if (request.descricao() == null || request.descricao().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A descrição do status é obrigatória.");
    }

    private void copiar(MosiaStatusConfigRequest request, MosiaStatusConfiguracao entity) {
        entity.setIdStatusMosia(request.idStatusMosia());
        entity.setIdTipoOcorrenciaMosia(request.idTipoOcorrenciaMosia());
        entity.setDescricao(request.descricao().trim());
        entity.setAtivo(request.ativo() == null || request.ativo());
        entity.setUltimaIdentificacao(OffsetDateTime.now());
    }

    private MosiaStatusCatalogoResponse toResponse(MosiaStatusConfiguracao entity) {
        return new MosiaStatusCatalogoResponse(entity.getId(), entity.getIdStatusMosia(),
                entity.getIdTipoOcorrenciaMosia(), entity.getDescricao(), entity.isAtivo(), entity.getOrigem(),
                entity.getPrimeiraIdentificacao(), entity.getUltimaIdentificacao());
    }
}
