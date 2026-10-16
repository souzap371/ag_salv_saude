package com.crm.integration.controller;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.mapper.OcorrenciaMapper;
import com.crm.integration.model.AlterarDatasRequest;
import com.crm.integration.model.Anotacao;
import com.crm.integration.model.AnotacaoRequest;
import com.crm.integration.model.Beneficiario;
import com.crm.integration.model.MosiaCallbackRequest;
import com.crm.integration.model.MosiaStatusUpdateRequest;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaResponse;
import com.crm.integration.model.UltimosProtocolosResponse;
import com.crm.integration.repository.BeneficiarioRepository;
import com.crm.integration.repository.OcorrenciaRepository;
import com.crm.integration.service.OcorrenciaClassificacaoService;
import com.crm.integration.service.OcorrenciaAnexoService;
import com.crm.integration.service.MosiaStatusConfiguracaoService;
import com.crm.integration.service.OcorrenciaAtualizacaoService;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/clientes/ocorrencias")
public class OcorrenciaController {

        @Value("${mosia.token.ocorrencia}")
        private String mosiaToken;

        private static final Logger log = LoggerFactory.getLogger(OcorrenciaController.class);

        @Autowired
        private OcorrenciaRepository ocorrenciaRepository;

        @Autowired
        private BeneficiarioRepository beneficiarioRepository;

        @Autowired
        private OcorrenciaClassificacaoService ocorrenciaClassificacaoService;

        @Autowired
        private OcorrenciaAnexoService ocorrenciaAnexoService;

        @Autowired
        private MosiaStatusConfiguracaoService mosiaStatusConfiguracaoService;

        @Autowired
        private OcorrenciaAtualizacaoService ocorrenciaAtualizacaoService;

        // CRIAR OCORRENCIA
        @PostMapping
        @Transactional
        public ResponseEntity<OcorrenciaResponse> criarOcorrencia(
                        @RequestHeader("Authorization") String authorization,
                        @RequestBody Ocorrencia ocorrencia) {
                if (!isValidToken(authorization)) {
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(new OcorrenciaResponse(
                                                        false,
                                                        "Token de autorização inválido.",
                                                        401,
                                                        System.currentTimeMillis(),
                                                        null));
                }
                ocorrencia.setDataRegistro(LocalDateTime.now());
                ocorrencia.setDataAtualizacao(LocalDateTime.now());
                ocorrenciaClassificacaoService.classificar(ocorrencia);
                salvarOuAtualizarBeneficiario(ocorrencia);
                Ocorrencia saved = ocorrenciaRepository.save(ocorrencia);
                return ResponseEntity.ok(new OcorrenciaResponse(
                                true,
                                "Ocorrência criada com sucesso.",
                                200,
                                System.currentTimeMillis(),
                                saved));
        }

        @PostMapping("/mosia")
        @Transactional
        public ResponseEntity<OcorrenciaResponse> receberOcorrenciaDoMosia(
                        @RequestHeader("Authorization") String authorization,
                        @RequestBody MosiaRequestDTO payload) {

                if (!isValidToken(authorization)) {
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(new OcorrenciaResponse(
                                                        false,
                                                        "Token de autorização inválido.",
                                                        401,
                                                        System.currentTimeMillis(),
                                                        null));
                }

                try {
                        // Mapeia o DTO do Mosia para a entidade Ocorrencia
                        Ocorrencia ocorrencia = OcorrenciaMapper.fromMosia(payload.getOcorrencia());
                        salvarOuAtualizarBeneficiario(ocorrencia);

                        // Define a funcionalidade da ocorrência (vinda do payload)
                        if (payload.getFuncionalidade() != null && payload.getFuncionalidade().getId() != null) {
                                String codigoFuncionalidade = payload.getFuncionalidade().getId().trim();
                                if (!codigoFuncionalidade.isEmpty()) {
                                        ocorrencia.setIdFuncionalidade(codigoFuncionalidade);
                                }
                        }

                        // Define data de registro e atualização
                        if (ocorrencia.getDataRegistro() == null) ocorrencia.setDataRegistro(LocalDateTime.now());
                        if (ocorrencia.getDataAtualizacao() == null) ocorrencia.setDataAtualizacao(LocalDateTime.now());

                        ocorrenciaClassificacaoService.classificarERegistrar(ocorrencia, payload);
                        if (payload.getOcorrencia() != null
                                        && payload.getOcorrencia().getAtendimento() != null
                                        && payload.getOcorrencia().getAtendimento().getStatus() != null) {
                                var statusRecebido = payload.getOcorrencia().getAtendimento().getStatus();
                                mosiaStatusConfiguracaoService.registrarObservado(
                                                ocorrencia.getIdTipoOcorrencia(),
                                                statusRecebido.getId(),
                                                statusRecebido.getDescricao());
                        }
                        ocorrenciaAnexoService.importarAnexosDosFormularios(ocorrencia);

                        // Salva a ocorrência no banco
                        Ocorrencia saved = ocorrenciaRepository.save(ocorrencia);

                        return ResponseEntity.ok(new OcorrenciaResponse(
                                        true,
                                        "Ocorrência do MOSIA criada com sucesso.",
                                        200,
                                        System.currentTimeMillis(),
                                        saved));

                } catch (Exception e) {
                        log.error("Erro geral ao processar payload do MOSIA", e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new OcorrenciaResponse(
                                                        false,
                                                        "Erro interno ao salvar ocorrência.",
                                                        500,
                                                        System.currentTimeMillis(),
                                                        null));
                }
        }

        private void salvarOuAtualizarBeneficiario(Ocorrencia ocorrencia) {
                Beneficiario recebido = ocorrencia.getBeneficiario();
                if (recebido == null || recebido.getId() == null) {
                        return;
                }

                Beneficiario persistido = beneficiarioRepository.findById(recebido.getId())
                                .map(existente -> {
                                        if (recebido.getNome() != null && !recebido.getNome().isBlank()) {
                                                existente.setNome(recebido.getNome());
                                        }
                                        if (recebido.getEmail() != null && !recebido.getEmail().isBlank()) {
                                                existente.setEmail(recebido.getEmail());
                                        }
                                        if (recebido.getTelefone() != null && !recebido.getTelefone().isBlank()) {
                                                existente.setTelefone(recebido.getTelefone());
                                        }
                                        return existente;
                                })
                                .orElseGet(() -> beneficiarioRepository.save(recebido));

                ocorrencia.setBeneficiario(persistido);
        }

        @GetMapping("/{protocolo}")
        public ResponseEntity<OcorrenciaResponse> detalheOcorrencia(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable("protocolo") String protocolo) {

                if (!isValidToken(authorization)) {
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(new OcorrenciaResponse(
                                                        false,
                                                        "Token de autorização inválido.",
                                                        401,
                                                        System.currentTimeMillis(),
                                                        null));
                }

                List<Ocorrencia> ocorrencias = ocorrenciaRepository.findByProtocolo(protocolo);

                if (ocorrencias.isEmpty()) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(new OcorrenciaResponse(
                                                        false,
                                                        "Ocorrência não encontrada.",
                                                        404,
                                                        System.currentTimeMillis(),
                                                        null));
                }

                Ocorrencia ocorrencia = ocorrencias.get(0);

                return ResponseEntity.ok(
                                new OcorrenciaResponse(
                                                true,
                                                "Detalhes da ocorrência encontrados com sucesso.",
                                                200,
                                                System.currentTimeMillis(),
                                                ocorrencia));
        }

        // ATUALIZAR STATUS DA OCORRÊNCIA
        // @Transactional
        // @PutMapping("/{protocolo}/status")
        // public ResponseEntity<OcorrenciaResponse> atualizarStatus(
        // @RequestHeader("Authorization") String authorization,
        // @PathVariable("protocolo") String protocolo,
        // @RequestBody MosiaStatusUpdateRequest request) {

        // // 🔹 Valida o token
        // if (!isValidToken(authorization)) {
        // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        // .body(new OcorrenciaResponse(false, "Token de autorização inválido.", 401,
        // System.currentTimeMillis(), null));
        // }

        // // 🔹 Busca a autorização mais recente pelo protocolo
        // var autorizacaoOpt =
        // autorizacaoRepository.findTopByProtocoloOrderByCreatedAtDesc(protocolo);
        // if (autorizacaoOpt.isEmpty()) {
        // return ResponseEntity.status(HttpStatus.NOT_FOUND)
        // .body(new OcorrenciaResponse(false, "Autorização não encontrada.", 404,
        // System.currentTimeMillis(), null));
        // }

        // var autorizacao = autorizacaoOpt.get();

        // // 🔹 Traduz o código do status para o nome conforme MOSIA
        // String nomeStatus =
        // MosiaStatusEnum.getNomePorCodigo(request.getIdNovoStatus());

        // // 🔹 Atualiza localmente
        // autorizacao.setStatus(nomeStatus); // ex: "Atendimento Finalizado"
        // autorizacao.setUpdatedAt(java.time.OffsetDateTime.now());
        // autorizacaoRepository.saveAndFlush(autorizacao);

        // // 🔹 Pega a chave única do registro (para enviar ao MOSIA)
        // String chaveUnica = autorizacao.getChaveUnica();

        // // 🔹 Atualiza também no MOSIA
        // Integer statusId =
        // mosiaIntegrationService.mapearCodigoPorNomePublic(nomeStatus);

        // boolean mosiaOk = mosiaIntegrationService.atualizarStatusNoMosia(
        // protocolo,
        // statusId,
        // nomeStatus,
        // chaveUnica);

        // // 🔹 Monta mensagem e resposta
        // String mensagem = mosiaOk
        // ? "Status atualizado localmente e sincronizado com o MOSIA."
        // : "Status atualizado localmente, mas houve falha ao sincronizar com o
        // MOSIA.";

        // int code = mosiaOk ? 200 : 207;

        // return ResponseEntity.status(code)
        // .body(new OcorrenciaResponse(true, mensagem, code,
        // System.currentTimeMillis(), null));
        // }

        // @Transactional
        // @PutMapping("/{protocolo}/status")
        // public ResponseEntity<OcorrenciaResponse> atualizarStatus(
        // @RequestHeader("Authorization") String authorization,
        // @PathVariable("protocolo") String protocolo,
        // @RequestBody MosiaStatusUpdateRequest request) {

        // if (!isValidToken(authorization)) {
        // return ResponseEntity
        // .status(HttpStatus.UNAUTHORIZED)
        // .body(new OcorrenciaResponse(
        // false,
        // "Token de autorização inválido.",
        // 401,
        // System.currentTimeMillis(),
        // null));
        // }

        // if (request == null || request.getIdNovoStatus() == null) {
        // return ResponseEntity
        // .badRequest()
        // .body(new OcorrenciaResponse(
        // false,
        // "O campo idNovoStatus é obrigatório.",
        // 400,
        // System.currentTimeMillis(),
        // null));
        // }

        // var autorizacaoOpt = autorizacaoRepository
        // .findTopByProtocoloOrderByCreatedAtDesc(protocolo);

        // if (autorizacaoOpt.isEmpty()) {
        // return ResponseEntity
        // .status(HttpStatus.NOT_FOUND)
        // .body(new OcorrenciaResponse(
        // false,
        // "Autorização não encontrada.",
        // 404,
        // System.currentTimeMillis(),
        // null));
        // }

        // AutorizacaoSolicitada autorizacao = autorizacaoOpt.get();

        // Integer statusId = request.getIdNovoStatus();

        // String nomeStatus = MosiaStatusEnum.getNomePorCodigo(statusId);

        // if (nomeStatus == null || nomeStatus.isBlank()) {
        // nomeStatus = request.getTextoDesfecho();
        // }

        // if (nomeStatus == null || nomeStatus.isBlank()) {
        // nomeStatus = String.valueOf(statusId);
        // }

        // String chaveUnica = autorizacao.getChaveUnica();

        // /*
        // * Primeiro envia para a Mosia.
        // */
        // boolean mosiaOk = mosiaIntegrationService.atualizarStatusNoMosia(
        // protocolo,
        // statusId,
        // nomeStatus,
        // chaveUnica);

        // /*
        // * Depois atualiza o registro local conforme o resultado.
        // */
        // autorizacao.setStatus(nomeStatus);
        // autorizacao.setStatusId(String.valueOf(statusId));
        // autorizacao.setUpdatedAt(
        // java.time.OffsetDateTime.now(
        // java.time.ZoneId.of("America/Sao_Paulo")));

        // if (mosiaOk) {
        // autorizacao.setSincronizadoMosia("S");

        // autorizacao.setUltimaSincronizacao(
        // java.time.OffsetDateTime.now(
        // java.time.ZoneId.of("America/Sao_Paulo")));
        // } else {
        // /*
        // * Mantém como pendente para o scheduler tentar novamente.
        // */
        // autorizacao.setSincronizadoMosia("N");
        // }

        // autorizacaoRepository.saveAndFlush(autorizacao);

        // String mensagem = mosiaOk
        // ? "Status atualizado localmente e sincronizado com o MOSIA."
        // : "Status atualizado localmente, mas ficou pendente de sincronização com o
        // MOSIA.";

        // int code = mosiaOk ? 200 : 207;

        // return ResponseEntity
        // .status(code)
        // .body(new OcorrenciaResponse(
        // true,
        // mensagem,
        // code,
        // System.currentTimeMillis(),
        // null));
        // }

        @Transactional
        @PutMapping("/{protocolo}/status")
        public ResponseEntity<OcorrenciaResponse> atualizarStatus(
                        @RequestHeader("Authorization") String authorization,

                        @PathVariable("protocolo") String protocolo,

                        @RequestBody MosiaStatusUpdateRequest request) {

                if (!isValidToken(authorization)) {
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(
                                                        new OcorrenciaResponse(
                                                                        false,
                                                                        "Token de autorização inválido.",
                                                                        401,
                                                                        System.currentTimeMillis(),
                                                                        null));
                }

                if (request == null
                                || request.getIdNovoStatus() == null) {

                        return ResponseEntity
                                        .badRequest()
                                        .body(
                                                        new OcorrenciaResponse(
                                                                        false,
                                                                        "O campo idNovoStatus é obrigatório.",
                                                                        400,
                                                                        System.currentTimeMillis(),
                                                                        null));
                }

                String protocoloNormalizado = normalizar(protocolo);

                if (protocoloNormalizado == null) {
                        return ResponseEntity
                                        .badRequest()
                                        .body(
                                                        new OcorrenciaResponse(
                                                                        false,
                                                                        "O protocolo é obrigatório.",
                                                                        400,
                                                                        System.currentTimeMillis(),
                                                                        null));
                }

                Integer statusId = request.getIdNovoStatus();
                try {
                        var resultado = ocorrenciaAtualizacaoService.atualizarStatus(
                                        protocoloNormalizado,
                                        statusId,
                                        request.getTextoDesfecho(),
                                        request.getChaveUnicaSolicitante(),
                                        request.getNumeroGuia());
                        int codigo = resultado.sincronizadoMosia() ? 200 : 207;
                        String mensagem = resultado.sincronizadoMosia()
                                        ? "Status sincronizado com o MOSIA e mensagem registrada para envio."
                                        : "Status atualizado e mensagem registrada, mas a sincronização com o MOSIA falhou.";
                        return ResponseEntity.status(codigo).body(new OcorrenciaResponse(
                                        true, mensagem, codigo, System.currentTimeMillis(), resultado.ocorrencia()));
                } catch (OcorrenciaAtualizacaoService.OcorrenciaNaoEncontradaException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OcorrenciaResponse(
                                        false, e.getMessage(), 404, System.currentTimeMillis(), null));
                } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body(new OcorrenciaResponse(
                                        false, e.getMessage(), 400, System.currentTimeMillis(), null));
                }
        }

        // @PostMapping("/status/callback")
        // @Transactional
        // public ResponseEntity<Void> receberCallbackStatus(
        // @RequestBody MosiaCallbackRequest callback) {

        // try {
        // log.info("Callback de status recebido do MOSIA.");

        // if (callback == null || callback.getOcorrencia() == null) {
        // log.warn("Callback ignorado: ocorrência não informada.");
        // return ResponseEntity.badRequest().build();
        // }

        // var ocorrencia = callback.getOcorrencia();

        // if (ocorrencia.getAtendimento() == null
        // || ocorrencia.getAtendimento().getStatus() == null) {

        // log.warn(
        // "Callback ignorado: atendimento ou status ausente. protocolo={}",
        // ocorrencia.getProtocolo());

        // return ResponseEntity.badRequest().build();
        // }

        // String protocolo = normalizar(ocorrencia.getProtocolo());

        // String chaveUnica = ocorrencia.getSolicitante() != null
        // ? somenteNumeros(
        // ocorrencia.getSolicitante().getChaveUnica())
        // : null;

        // String idStatus = ocorrencia.getAtendimento().getStatus().getId();

        // String nomeStatus = ocorrencia.getAtendimento().getStatus().getDescricao();

        // if (protocolo == null || protocolo.isBlank()) {
        // log.warn("Callback ignorado: protocolo não informado.");
        // return ResponseEntity.badRequest().build();
        // }

        // log.info(
        // "Processando callback MOSIA: protocolo={} statusId={} status={} chave={}",
        // protocolo,
        // idStatus,
        // nomeStatus,
        // mascararChave(chaveUnica));

        // var autorizacaoOpt = autorizacaoRepository
        // .findTopByProtocoloOrderByCreatedAtDesc(protocolo);

        // AutorizacaoSolicitada autorizacao;

        // if (autorizacaoOpt.isPresent()) {
        // autorizacao = autorizacaoOpt.get();

        // /*
        // * A chave recebida no callback vem da própria ocorrência
        // * existente no MOSIA. Portanto, deve atualizar a chave local.
        // */
        // if (chaveUnica != null && !chaveUnica.isBlank()) {
        // autorizacao.setChaveUnica(chaveUnica);
        // }

        // autorizacao.setStatus(nomeStatus);
        // autorizacao.setStatusId(idStatus);
        // autorizacao.setSincronizadoMosia("S");

        // autorizacao.setUltimaSincronizacao(
        // java.time.OffsetDateTime.now(
        // java.time.ZoneId.of("America/Sao_Paulo")));

        // autorizacao.setUpdatedAt(
        // java.time.OffsetDateTime.now(
        // java.time.ZoneId.of("America/Sao_Paulo")));

        // autorizacaoRepository.saveAndFlush(autorizacao);

        // log.info(
        // "Autorização atualizada pelo callback: protocolo={}",
        // protocolo);

        // } else {
        // autorizacao = new AutorizacaoSolicitada();

        // autorizacao.setProtocolo(protocolo);
        // autorizacao.setChaveUnica(chaveUnica);
        // autorizacao.setStatus(nomeStatus);
        // autorizacao.setStatusId(idStatus);
        // autorizacao.setSincronizadoMosia("S");

        // autorizacao.setUltimaSincronizacao(
        // java.time.OffsetDateTime.now(
        // java.time.ZoneId.of("America/Sao_Paulo")));

        // autorizacao.setCreatedAt(
        // java.time.OffsetDateTime.now(
        // java.time.ZoneId.of("America/Sao_Paulo")));

        // autorizacao.setUpdatedAt(
        // java.time.OffsetDateTime.now(
        // java.time.ZoneId.of("America/Sao_Paulo")));

        // autorizacaoRepository.saveAndFlush(autorizacao);

        // log.info(
        // "Nova autorização criada pelo callback: protocolo={}",
        // protocolo);
        // }

        // return ResponseEntity.ok().build();

        // } catch (Exception e) {
        // log.error(
        // "Erro ao processar callback de status do MOSIA.",
        // e);

        // return ResponseEntity
        // .status(HttpStatus.INTERNAL_SERVER_ERROR)
        // .build();
        // }
        // }

        @PostMapping("/status/callback")
        @Transactional
        public ResponseEntity<Void> receberCallbackStatus(
                        @RequestBody MosiaCallbackRequest callback) {

                try {
                        log.info(
                                        "[MOSIA CALLBACK] Callback de status recebido.");

                        if (callback == null
                                        || callback.getOcorrencia() == null) {

                                log.warn(
                                                "[MOSIA CALLBACK] "
                                                                + "Ocorrência não informada.");

                                return ResponseEntity
                                                .badRequest()
                                                .build();
                        }

                        var ocorrencia = callback.getOcorrencia();

                        if (ocorrencia.getAtendimento() == null
                                        || ocorrencia
                                                        .getAtendimento()
                                                        .getStatus() == null) {

                                log.warn(
                                                "[MOSIA CALLBACK] "
                                                                + "Atendimento ou status ausente. "
                                                                + "protocolo={}",
                                                ocorrencia.getProtocolo());

                                return ResponseEntity
                                                .badRequest()
                                                .build();
                        }

                        String protocolo = normalizar(
                                        ocorrencia.getProtocolo());

                        if (protocolo == null) {
                                log.warn(
                                                "[MOSIA CALLBACK] "
                                                                + "Protocolo não informado.");

                                return ResponseEntity
                                                .badRequest()
                                                .build();
                        }

                        String chaveUnica = null;

                        if (ocorrencia.getSolicitante() != null) {
                                chaveUnica = somenteNumeros(
                                                ocorrencia
                                                                .getSolicitante()
                                                                .getChaveUnica());
                        }

                        String statusId = normalizar(
                                        ocorrencia
                                                        .getAtendimento()
                                                        .getStatus()
                                                        .getId());

                        String nomeStatus = normalizar(
                                        ocorrencia
                                                        .getAtendimento()
                                                        .getStatus()
                                                        .getDescricao());

                        Integer idTipoOcorrencia = ocorrenciaRepository.findByProtocolo(protocolo).stream()
                                        .findFirst()
                                        .map(Ocorrencia::getIdTipoOcorrencia)
                                        .filter(id -> id > 0)
                                        .orElse(null);
                        mosiaStatusConfiguracaoService.registrarObservado(
                                        idTipoOcorrencia,
                                        statusId,
                                        nomeStatus);

                        log.info(
                                        "[MOSIA CALLBACK] Processando. "
                                                        + "protocolo={} statusId={} status={} chave={}",
                                        protocolo,
                                        statusId,
                                        nomeStatus,
                                        mascararChave(chaveUnica));

                        ocorrenciaAtualizacaoService.registrarStatusRecebidoDoMosia(
                                        protocolo, statusId, nomeStatus);

                        log.info(
                                        "[MOSIA CALLBACK] Processado com sucesso. "
                                                        + "protocolo={} statusId={} "
                                                        + "origem=MOSIA",
                                        protocolo,
                                        statusId);

                        return ResponseEntity
                                        .ok()
                                        .build();

                } catch (Exception e) {
                        log.error(
                                        "[MOSIA CALLBACK] "
                                                        + "Erro ao processar callback de status.",
                                        e);

                        return ResponseEntity
                                        .status(
                                                        HttpStatus.INTERNAL_SERVER_ERROR)
                                        .build();
                }
        }

        private String normalizar(String valor) {
                if (valor == null) {
                        return null;
                }

                String normalizado = valor.trim();
                return normalizado.isEmpty() ? null : normalizado;
        }

        private String somenteNumeros(String valor) {
                if (valor == null) {
                        return null;
                }

                return valor.replaceAll("\\D", "");
        }

        private String mascararChave(String chave) {
                if (chave == null || chave.length() < 4) {
                        return "***";
                }

                return "***" + chave.substring(chave.length() - 4);
        }

        // CRIAR ANOTAÇÃO
        @PostMapping("/{protocolo}/anotacao")
        public ResponseEntity<OcorrenciaResponse> criarAnotacao(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable("protocolo") String protocolo,
                        @RequestBody AnotacaoRequest request) {

                if (!isValidToken(authorization)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new OcorrenciaResponse(false, "Token de autorização inválido.", 401,
                                                        System.currentTimeMillis(), null));
                }

                List<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (ocorrenciaOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new OcorrenciaResponse(false, "Ocorrência não encontrada.", 404,
                                                        System.currentTimeMillis(), null));
                }

                Ocorrencia ocorrencia = ocorrenciaOpt.get(0);

                // Criar nova anotação
                Anotacao anotacao = new Anotacao();
                anotacao.setTexto(request.getAnotacao());
                anotacao.setDataCriacao(LocalDateTime.now());
                anotacao.setOcorrencia(ocorrencia);

                // Adiciona na lista
                ocorrencia.getAnotacoes().add(anotacao);
                ocorrencia.setDataAtualizacao(LocalDateTime.now());

                Ocorrencia updatedOcorrencia = ocorrenciaRepository.save(ocorrencia);

                return ResponseEntity.ok(new OcorrenciaResponse(
                                true,
                                "Anotação criada com sucesso.",
                                200,
                                System.currentTimeMillis(),
                                updatedOcorrencia));
        }

        // ATIVAR OCORRENCIA
        @PutMapping("/{protocolo}/ativar")
        public ResponseEntity<OcorrenciaResponse> ativarOcorrencia(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable("protocolo") String protocolo) {

                if (!isValidToken(authorization)) {
                        OcorrenciaResponse response = new OcorrenciaResponse(
                                        false,
                                        "Token de autorização inválido.",
                                        401,
                                        System.currentTimeMillis(),
                                        null);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                List<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (ocorrenciaOpt.isEmpty()) {
                        OcorrenciaResponse response = new OcorrenciaResponse(
                                        false,
                                        "Ocorrência não encontrada.",
                                        404,
                                        System.currentTimeMillis(),
                                        null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                Ocorrencia ocorrencia = ocorrenciaOpt.get(0);

                if (Boolean.FALSE.equals(ocorrencia.getRascunho())) {
                        OcorrenciaResponse response = new OcorrenciaResponse(
                                        false,
                                        "A ocorrência já está ativa.",
                                        400,
                                        System.currentTimeMillis(),
                                        null);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }

                ocorrencia.setRascunho(false); // Ativa a ocorrência
                ocorrencia.setDataAtualizacao(LocalDateTime.now());
                Ocorrencia updated = ocorrenciaRepository.save(ocorrencia);

                OcorrenciaResponse response = new OcorrenciaResponse(
                                true,
                                "Ocorrência ativada com sucesso.",
                                200,
                                System.currentTimeMillis(),
                                updated);
                return ResponseEntity.ok(response);
        }

        // LISTAR ÚLTIMOS PROTOCOLOS
        @GetMapping("/ultimos-protocolos")
        public ResponseEntity<UltimosProtocolosResponse> listarUltimosProtocolos(
                        @RequestHeader("Authorization") String authorization) {

                // Validação do token de autorização
                if (!isValidToken(authorization)) {
                        UltimosProtocolosResponse response = new UltimosProtocolosResponse(
                                        false,
                                        "Token de autorização inválido.",
                                        401,
                                        System.currentTimeMillis(),
                                        null);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                // Buscar os 10 últimos registros ordenados pela data de registro
                List<Ocorrencia> ultimasOcorrencias = ocorrenciaRepository.findTop10ByOrderByDataRegistroDesc();
                // Extraindo os protocolos
                List<String> protocolos = ultimasOcorrencias.stream()
                                .map(Ocorrencia::getProtocolo)
                                .collect(Collectors.toList());

                UltimosProtocolosResponse response = new UltimosProtocolosResponse(
                                true,
                                "Protocolos listados com sucesso.",
                                200,
                                System.currentTimeMillis(),
                                protocolos);
                return ResponseEntity.ok(response);
        }

        // ALTERAR DATAS DA OCORRÊNCIA
        @PutMapping("/{protocolo}/datas")
        public ResponseEntity<OcorrenciaResponse> alterarDatas(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable("protocolo") String protocolo,
                        @RequestBody AlterarDatasRequest request) {

                if (!isValidToken(authorization)) {
                        OcorrenciaResponse response = new OcorrenciaResponse(
                                        false,
                                        "Token de autorização inválido.",
                                        401,
                                        System.currentTimeMillis(),
                                        null);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                List<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (ocorrenciaOpt.isEmpty()) {
                        OcorrenciaResponse response = new OcorrenciaResponse(
                                        false,
                                        "Ocorrência não encontrada.",
                                        404,
                                        System.currentTimeMillis(),
                                        null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                Ocorrencia ocorrencia = ocorrenciaOpt.get(0);

                // Converter timestamp para LocalDateTime e atualizar os campos, se enviados
                if (request.getDataRegistro() != null) {
                        LocalDateTime novaDataRegistro = Instant.ofEpochMilli(request.getDataRegistro())
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                        ocorrencia.setDataRegistro(novaDataRegistro);
                }

                if (request.getDataAtualizacao() != null) {
                        LocalDateTime novaDataAtualizacao = Instant.ofEpochMilli(request.getDataAtualizacao())
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
                        ocorrencia.setDataAtualizacao(novaDataAtualizacao);
                }

                Ocorrencia updatedOcorrencia = ocorrenciaRepository.save(ocorrencia);

                OcorrenciaResponse response = new OcorrenciaResponse(
                                true,
                                "Datas da ocorrência atualizadas com sucesso.",
                                200,
                                System.currentTimeMillis(),
                                updatedOcorrencia);
                return ResponseEntity.ok(response);
        }

        // Método para validação do token (único)
        private boolean isValidToken(String token) {
                if (token == null || token.isEmpty()) {
                        return false;
                }

                return token.equals(mosiaToken);
        }

}
