package com.crm.integration.controller;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.enums.MosiaStatusEnum;
import com.crm.integration.mapper.OcorrenciaMapper;
import com.crm.integration.model.AlterarDatasRequest;
import com.crm.integration.model.Anotacao;
import com.crm.integration.model.AnotacaoRequest;
import com.crm.integration.model.AutorizacaoSolicitada;
import com.crm.integration.model.MosiaCallbackRequest;
import com.crm.integration.model.MosiaStatusUpdateRequest;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaResponse;
import com.crm.integration.model.UltimosProtocolosResponse;
import com.crm.integration.repository.AutorizacaoRepository;
import com.crm.integration.repository.OcorrenciaRepository;
import com.crm.integration.service.MosiaIntegrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import java.sql.Timestamp;
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
        private ObjectMapper objectMapper;

        // CRIAR OCORRENCIA
        @PostMapping
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
                // 1) seu fluxo normal de validação e save
                ocorrencia.setDataRegistro(LocalDateTime.now());
                ocorrencia.setDataAtualizacao(LocalDateTime.now());
                Ocorrencia saved = ocorrenciaRepository.save(ocorrencia);

                // 2) agora serializa e insere na tabela de BLOBs

                try {
                        byte[] json = objectMapper.writeValueAsBytes(saved);
                        Timestamp dtCarga = Timestamp.valueOf(saved.getDataRegistro());
                        Timestamp dtProcessa = Timestamp.valueOf(saved.getDataAtualizacao());
                        ocorrenciaRepository.saveBlob(
                                        saved.getId(),
                                        json,
                                        dtCarga,
                                        dtProcessa);

                } catch (JsonProcessingException e) {
                        log.error("Erro ao serializar Ocorrencia para BLOB", e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new OcorrenciaResponse(
                                                        false,
                                                        "Erro interno ao processar a ocorrência.",
                                                        500,
                                                        System.currentTimeMillis(),
                                                        null));
                }
                // 4) Resposta de sucesso
                return ResponseEntity.ok(new OcorrenciaResponse(
                                true,
                                "Ocorrência criada e blob salvo com sucesso.",
                                200,
                                System.currentTimeMillis(),
                                saved));
        }

        @PostMapping("/mosia")
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

                        // Define a funcionalidade da ocorrência (vinda do payload)
                        // ocorrencia.setIdFuncionalidade(payload.getFuncionalidade().getId().intValue());
                        if (payload.getFuncionalidade() != null && payload.getFuncionalidade().getId() != null) {
                                ocorrencia.setIdFuncionalidade(Integer.parseInt(payload.getFuncionalidade().getId()));
                        }

                        // Define data de registro e atualização
                        ocorrencia.setDataRegistro(LocalDateTime.now());
                        ocorrencia.setDataAtualizacao(LocalDateTime.now());

                        // Salva a ocorrência no banco
                        Ocorrencia saved = ocorrenciaRepository.save(ocorrencia);

                        // Serializa e salva como BLOB
                        byte[] json = objectMapper.writeValueAsBytes(saved);
                        Timestamp dtCarga = Timestamp.valueOf(saved.getDataRegistro());
                        Timestamp dtProcessa = Timestamp.valueOf(saved.getDataAtualizacao());

                        ocorrenciaRepository.saveBlob(
                                        saved.getId(),
                                        json,
                                        dtCarga,
                                        dtProcessa);

                        return ResponseEntity.ok(new OcorrenciaResponse(
                                        true,
                                        "Ocorrência do MOSIA criada e blob salvo com sucesso.",
                                        200,
                                        System.currentTimeMillis(),
                                        saved));

                } catch (JsonProcessingException e) {
                        log.error("Erro ao serializar Ocorrencia para BLOB", e);
                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new OcorrenciaResponse(
                                                        false,
                                                        "Erro interno ao processar a ocorrência.",
                                                        500,
                                                        System.currentTimeMillis(),
                                                        null));
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

        // DETALHE DA OCORRÊNCIA
        @GetMapping("/{protocolo}")
        public ResponseEntity<OcorrenciaResponse> detalheOcorrencia(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable("protocolo") String protocolo) {

                // Validação do token de autorização (implemente a lógica conforme sua
                // necessidade)
                if (!isValidToken(authorization)) {
                        OcorrenciaResponse response = new OcorrenciaResponse(false, "Token de autorização inválido.",
                                        401,
                                        System.currentTimeMillis(), null);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                // Buscar a ocorrência pelo protocolo informado
                List<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (ocorrenciaOpt.isEmpty()) {
                        Ocorrencia ocorrencia = ocorrenciaOpt.get(0);
                        OcorrenciaResponse response = new OcorrenciaResponse(true,
                                        "Detalhes da ocorrência encontrados com sucesso.", 200,
                                        System.currentTimeMillis(), ocorrencia);
                        return ResponseEntity.ok(response);
                } else {
                        OcorrenciaResponse response = new OcorrenciaResponse(false, "Ocorrência não encontrada.", 404,
                                        System.currentTimeMillis(), null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
        }

        // ATUALIZAR STATUS DA OCORRÊNCIA
        @Autowired
        private AutorizacaoRepository autorizacaoRepository;

        @Autowired
        private MosiaIntegrationService mosiaIntegrationService;

        @Transactional
        @PutMapping("/{protocolo}/status")
        public ResponseEntity<OcorrenciaResponse> atualizarStatus(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable("protocolo") String protocolo,
                        @RequestBody MosiaStatusUpdateRequest request) {

                // 🔹 Valida o token
                if (!isValidToken(authorization)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(new OcorrenciaResponse(false, "Token de autorização inválido.", 401,
                                                        System.currentTimeMillis(), null));
                }

                // 🔹 Busca a autorização mais recente pelo protocolo
                var autorizacaoOpt = autorizacaoRepository.findTopByProtocoloOrderByCreatedAtDesc(protocolo);
                if (autorizacaoOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new OcorrenciaResponse(false, "Autorização não encontrada.", 404,
                                                        System.currentTimeMillis(), null));
                }

                var autorizacao = autorizacaoOpt.get();

                // 🔹 Traduz o código do status para o nome conforme MOSIA
                String nomeStatus = MosiaStatusEnum.getNomePorCodigo(request.getIdNovoStatus());

                // 🔹 Atualiza localmente
                autorizacao.setStatus(nomeStatus); // ex: "Atendimento Finalizado"
                autorizacao.setUpdatedAt(java.time.OffsetDateTime.now());
                autorizacaoRepository.saveAndFlush(autorizacao);

                // 🔹 Pega a chave única do registro (para enviar ao MOSIA)
                String chaveUnica = autorizacao.getChaveUnica();

                // 🔹 Atualiza também no MOSIA
                boolean mosiaOk = mosiaIntegrationService.atualizarStatusNoMosia(protocolo, nomeStatus, chaveUnica);

                // 🔹 Monta mensagem e resposta
                String mensagem = mosiaOk
                                ? "Status atualizado localmente e sincronizado com o MOSIA."
                                : "Status atualizado localmente, mas houve falha ao sincronizar com o MOSIA.";

                int code = mosiaOk ? 200 : 207;

                return ResponseEntity.status(code)
                                .body(new OcorrenciaResponse(true, mensagem, code, System.currentTimeMillis(), null));
        }

        @PostMapping("/status/callback")
        public ResponseEntity<Void> receberCallbackStatus(@RequestBody MosiaCallbackRequest callback) {
                try {
                        System.out.println("📩 Callback recebido do MOSIA:");
                        System.out.println(
                                        new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(callback));

                        var ocorrencia = callback.getOcorrencia();
                        if (ocorrencia == null || ocorrencia.getAtendimento() == null) {
                                System.out.println("⚠️ Callback sem dados de ocorrência/atendimento válidos.");
                                return ResponseEntity.ok().build();
                        }

                        String protocolo = ocorrencia.getProtocolo();
                        String chaveUnica = ocorrencia.getSolicitante() != null
                                        ? ocorrencia.getSolicitante().getChaveUnica()
                                        : null;
                        String idStatus = ocorrencia.getAtendimento().getStatus().getId();
                        String nomeStatus = ocorrencia.getAtendimento().getStatus().getDescricao();

                        System.out.printf("🔎 Atualizando protocolo=%s | chaveUnica=%s | status=%s (%s)%n",
                                        protocolo, chaveUnica, nomeStatus, idStatus);

                        var autorizacaoOpt = autorizacaoRepository.findTopByProtocoloOrderByCreatedAtDesc(protocolo);

                        if (autorizacaoOpt.isPresent()) {
                                // 🔹 Atualiza registro existente
                                var autorizacao = autorizacaoOpt.get();
                                autorizacao.setStatus(nomeStatus);
                                autorizacao.setStatusId(idStatus);
                                autorizacao.setUpdatedAt(java.time.OffsetDateTime.now());
                                autorizacaoRepository.saveAndFlush(autorizacao);

                                System.out.println("✅ Atualizado com sucesso no banco local!");
                                System.out.printf("🧩 Gravado no banco: status=%s | status_id=%s%n",
                                                autorizacao.getStatus(), autorizacao.getStatusId());
                        } else {
                                // 🔹 Cria um novo registro se não existir
                                var nova = new AutorizacaoSolicitada();
                                nova.setProtocolo(protocolo);
                                nova.setChaveUnica(chaveUnica);
                                nova.setStatus(nomeStatus);
                                nova.setStatusId(String.valueOf(idStatus));
                                nova.setCreatedAt(java.time.OffsetDateTime.now());
                                nova.setUpdatedAt(java.time.OffsetDateTime.now());
                                nova.setSincronizadoMosia("S");

                                autorizacaoRepository.saveAndFlush(nova);

                                System.out.println("🆕 Novo registro criado com sucesso!");
                                System.out.printf("🔎 Atualizando protocolo=%s | chaveUnica=%s | status=%s (%s)%n",
                                                protocolo, chaveUnica, nomeStatus, idStatus);

                        }

                        return ResponseEntity.ok().build();

                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
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
