package com.crm.integration.controller;

import com.crm.integration.dto.MosiaRequestDTO;
import com.crm.integration.mapper.OcorrenciaMapper;
import com.crm.integration.model.AlterarDatasRequest;
import com.crm.integration.model.Anotacao;
import com.crm.integration.model.AnotacaoRequest;
import com.crm.integration.model.Ocorrencia;
import com.crm.integration.model.OcorrenciaResponse;
import com.crm.integration.model.StatusUpdateRequest;
import com.crm.integration.model.UltimosProtocolosResponse;
import com.crm.integration.repository.OcorrenciaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
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

        @Value("${mosia.token}")
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
                        ocorrencia.setIdFuncionalidade(payload.getFuncionalidade().getId().intValue());

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
                Optional<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (ocorrenciaOpt.isPresent()) {
                        Ocorrencia ocorrencia = ocorrenciaOpt.get();
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
        @PutMapping("/{protocolo}/status")
        public ResponseEntity<OcorrenciaResponse> atualizarStatus(
                        @RequestHeader("Authorization") String authorization,
                        @PathVariable("protocolo") String protocolo,
                        @RequestBody StatusUpdateRequest request) {

                if (!isValidToken(authorization)) {
                        OcorrenciaResponse response = new OcorrenciaResponse(false, "Token de autorização inválido.",
                                        401, System.currentTimeMillis(), null);
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                Optional<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (!ocorrenciaOpt.isPresent()) {
                        OcorrenciaResponse response = new OcorrenciaResponse(false, "Ocorrência não encontrada.", 404,
                                        System.currentTimeMillis(), null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                Ocorrencia ocorrencia = ocorrenciaOpt.get();

                // Atualiza rascunho (se informado)
                if (request.getRascunho() != null) {
                        ocorrencia.setRascunho(request.getRascunho());
                }

                // Atualiza status (se informado)
                if (request.getStatus() != null && !request.getStatus().isEmpty()) {
                        ocorrencia.setStatus(request.getStatus());
                }

                ocorrencia.setDataAtualizacao(LocalDateTime.now());
                Ocorrencia updatedOcorrencia = ocorrenciaRepository.save(ocorrencia);

                OcorrenciaResponse response = new OcorrenciaResponse(true,
                                "Status da ocorrência atualizado com sucesso.", 200,
                                System.currentTimeMillis(), updatedOcorrencia);
                return ResponseEntity.ok(response);
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

                Optional<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (!ocorrenciaOpt.isPresent()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(new OcorrenciaResponse(false, "Ocorrência não encontrada.", 404,
                                                        System.currentTimeMillis(), null));
                }

                Ocorrencia ocorrencia = ocorrenciaOpt.get();

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

                Optional<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (!ocorrenciaOpt.isPresent()) {
                        OcorrenciaResponse response = new OcorrenciaResponse(
                                        false,
                                        "Ocorrência não encontrada.",
                                        404,
                                        System.currentTimeMillis(),
                                        null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                Ocorrencia ocorrencia = ocorrenciaOpt.get();

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

                Optional<Ocorrencia> ocorrenciaOpt = ocorrenciaRepository.findByProtocolo(protocolo);
                if (!ocorrenciaOpt.isPresent()) {
                        OcorrenciaResponse response = new OcorrenciaResponse(
                                        false,
                                        "Ocorrência não encontrada.",
                                        404,
                                        System.currentTimeMillis(),
                                        null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }

                Ocorrencia ocorrencia = ocorrenciaOpt.get();

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
