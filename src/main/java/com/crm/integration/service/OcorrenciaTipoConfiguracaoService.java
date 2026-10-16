package com.crm.integration.service;

import com.crm.integration.dto.OcorrenciaTipoConfigRequest;
import com.crm.integration.dto.OcorrenciaTipoConfigResponse;
import com.crm.integration.enums.TipoNegocioOcorrencia;
import com.crm.integration.model.OcorrenciaTipoConfiguracao;
import com.crm.integration.repository.OcorrenciaTipoConfiguracaoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class OcorrenciaTipoConfiguracaoService {

    private final OcorrenciaTipoConfiguracaoRepository repository;

    public OcorrenciaTipoConfiguracaoService(OcorrenciaTipoConfiguracaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<OcorrenciaTipoConfigResponse> listar() {
        return repository.findAll(Sort.by("tipoNegocio").ascending().and(Sort.by("id").ascending()))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OcorrenciaTipoConfigResponse criar(OcorrenciaTipoConfigRequest request) {
        validar(request);
        validarDuplicidade(null, request);
        OcorrenciaTipoConfiguracao entity = new OcorrenciaTipoConfiguracao();
        copiar(request, entity);
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public OcorrenciaTipoConfigResponse atualizar(Long id, OcorrenciaTipoConfigRequest request) {
        validar(request);
        OcorrenciaTipoConfiguracao entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuração não encontrada."));
        validarDuplicidade(id, request);
        copiar(request, entity);
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        OcorrenciaTipoConfiguracao entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Configuração não encontrada."));
        repository.delete(entity);
        repository.flush();
    }

    @Transactional
    public void inicializarTipoAutorizacoesSalv() {
        if (repository.existsByIdTipoOcorrencia(1895)) {
            return;
        }
        OcorrenciaTipoConfiguracao entity = new OcorrenciaTipoConfiguracao();
        entity.setTipoNegocio(TipoNegocioOcorrencia.SOLICITACAO_AUTORIZACAO.name());
        entity.setIdTipoOcorrencia(1895);
        entity.setAtivo(true);
        repository.save(entity);
    }

    private void validar(OcorrenciaTipoConfigRequest request) {
        if (request == null || request.tipoNegocio() == null || request.tipoNegocio().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O tipo de ocorrência é obrigatório.");
        }
        if (request.tipoNegocio().trim().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O tipo de ocorrência deve possuir no máximo 200 caracteres.");
        }
        validarCodigoFuncionalidade(request.idFuncionalidade());
        validarId(request.idTipoOcorrencia(), "ID do tipo de ocorrência");
        validarId(request.idAssunto(), "ID do assunto");
        if ((request.idFuncionalidade() == null || request.idFuncionalidade().isBlank())
                && request.idTipoOcorrencia() == null
                && request.idAssunto() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe pelo menos um ID do Mosia.");
        }
    }

    private void validarId(Integer id, String campo) {
        if (id != null && id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, campo + " deve ser maior que zero.");
        }
    }

    private void validarCodigoFuncionalidade(String codigo) {
        if (codigo == null || codigo.isBlank()) return;
        String normalizado = codigo.trim();
        if (normalizado.length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ID da funcionalidade deve possuir no máximo 100 caracteres.");
        }
        if (!normalizado.matches("[A-Za-z0-9._:-]+")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "ID da funcionalidade possui caracteres inválidos.");
        }
    }

    private void validarDuplicidade(Long idAtual, OcorrenciaTipoConfigRequest request) {
        String codigoNormalizado = normalizarCodigo(request.idFuncionalidade());
        boolean duplicada = repository.findAll().stream().anyMatch(config ->
                (idAtual == null || !Objects.equals(config.getId(), idAtual))
                        && Objects.equals(config.getTipoNegocio(), request.tipoNegocio().trim())
                        && Objects.equals(config.getIdFuncionalidade(), codigoNormalizado)
                        && Objects.equals(config.getIdTipoOcorrencia(), request.idTipoOcorrencia())
                        && Objects.equals(config.getIdAssunto(), request.idAssunto()));
        if (duplicada) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta associação de IDs já está cadastrada.");
        }
    }

    private void copiar(OcorrenciaTipoConfigRequest request, OcorrenciaTipoConfiguracao entity) {
        entity.setTipoNegocio(request.tipoNegocio().trim());
        entity.setIdFuncionalidade(normalizarCodigo(request.idFuncionalidade()));
        entity.setIdTipoOcorrencia(request.idTipoOcorrencia());
        entity.setIdAssunto(request.idAssunto());
        entity.setAtivo(request.ativo() == null || request.ativo());
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null || codigo.isBlank() ? null : codigo.trim();
    }

    private OcorrenciaTipoConfigResponse toResponse(OcorrenciaTipoConfiguracao entity) {
        return new OcorrenciaTipoConfigResponse(
                entity.getId(),
                entity.getTipoNegocio(),
                entity.getIdFuncionalidade(),
                entity.getIdTipoOcorrencia(),
                entity.getIdAssunto(),
                entity.isAtivo());
    }
}
