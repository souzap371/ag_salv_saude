package com.crm.integration.controller;


import com.crm.integration.model.AtualizaFilaRequest;
import com.crm.integration.model.CreateMeetingRequest;
import com.crm.integration.model.Meeting;
import com.crm.integration.model.MeetingResponse;
import com.crm.integration.repository.MeetingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/clientes/meeting")
public class MeetingController {

    @Autowired
    private MeetingRepository meetingRepository;

    // CRIAR MEETING
    @PostMapping
    public ResponseEntity<MeetingResponse> criarMeeting(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CreateMeetingRequest request) {

        if (!isValidToken(authorization)) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Token de autorização inválido.",
                    401,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Meeting meeting = new Meeting();
        // Se o meetingId não for enviado, gere-o (por exemplo, utilizando um UUID)
        meeting.setMeetingId(request.getMeetingId() != null ? request.getMeetingId() : java.util.UUID.randomUUID().toString());
        meeting.setAssunto(request.getAssunto());
        meeting.setDescricao(request.getDescricao());
        meeting.setStatus("criado");
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());

        Meeting savedMeeting = meetingRepository.save(meeting);

        MeetingResponse response = new MeetingResponse(
                true,
                "Meeting criado com sucesso.",
                200,
                System.currentTimeMillis(),
                savedMeeting);
        return ResponseEntity.ok(response);
    }

    // CANCELAMENTO DE MEETING
    @PutMapping("/{meetingId}/cancel")
    public ResponseEntity<MeetingResponse> cancelarMeeting(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("meetingId") String meetingId) {

        if (!isValidToken(authorization)) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Token de autorização inválido.",
                    401,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Meeting> meetingOpt = meetingRepository.findByMeetingId(meetingId);
        if (!meetingOpt.isPresent()) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Meeting não encontrado.",
                    404,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Meeting meeting = meetingOpt.get();
        meeting.setStatus("cancelado");
        meeting.setUpdatedAt(LocalDateTime.now());
        Meeting updatedMeeting = meetingRepository.save(meeting);

        MeetingResponse response = new MeetingResponse(
                true,
                "Meeting cancelado com sucesso.",
                200,
                System.currentTimeMillis(),
                updatedMeeting);
        return ResponseEntity.ok(response);
    }

    // ATUALIZAÇÃO DE FILA
    @PutMapping("/{meetingId}/fila")
    public ResponseEntity<MeetingResponse> atualizarFila(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("meetingId") String meetingId,
            @RequestBody AtualizaFilaRequest request) {

        if (!isValidToken(authorization)) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Token de autorização inválido.",
                    401,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Meeting> meetingOpt = meetingRepository.findByMeetingId(meetingId);
        if (!meetingOpt.isPresent()) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Meeting não encontrado.",
                    404,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Meeting meeting = meetingOpt.get();
        meeting.setFila(request.getNovaFila());
        meeting.setUpdatedAt(LocalDateTime.now());
        Meeting updatedMeeting = meetingRepository.save(meeting);

        MeetingResponse response = new MeetingResponse(
                true,
                "Fila atualizada com sucesso.",
                200,
                System.currentTimeMillis(),
                updatedMeeting);
        return ResponseEntity.ok(response);
    }

    // CANCELAMENTO DA CONSULTA
    @PutMapping("/{meetingId}/consulta/cancel")
    public ResponseEntity<MeetingResponse> cancelarConsulta(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("meetingId") String meetingId) {

        if (!isValidToken(authorization)) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Token de autorização inválido.",
                    401,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Meeting> meetingOpt = meetingRepository.findByMeetingId(meetingId);
        if (!meetingOpt.isPresent()) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Meeting não encontrado.",
                    404,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Meeting meeting = meetingOpt.get();
        // Atualize o status da consulta para "cancelada"
        meeting.setConsultaStatus("cancelada");
        meeting.setUpdatedAt(LocalDateTime.now());
        Meeting updatedMeeting = meetingRepository.save(meeting);

        MeetingResponse response = new MeetingResponse(
                true,
                "Consulta cancelada com sucesso.",
                200,
                System.currentTimeMillis(),
                updatedMeeting);
        return ResponseEntity.ok(response);
    }

    // ENCERRAMENTO DA CONSULTA
    @PutMapping("/{meetingId}/consulta/encerrar")
    public ResponseEntity<MeetingResponse> encerrarConsulta(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("meetingId") String meetingId) {

        if (!isValidToken(authorization)) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Token de autorização inválido.",
                    401,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Meeting> meetingOpt = meetingRepository.findByMeetingId(meetingId);
        if (!meetingOpt.isPresent()) {
            MeetingResponse response = new MeetingResponse(
                    false,
                    "Meeting não encontrado.",
                    404,
                    System.currentTimeMillis(),
                    null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Meeting meeting = meetingOpt.get();
        // Atualize o status da consulta para "encerrada"
        meeting.setConsultaStatus("encerrada");
        meeting.setUpdatedAt(LocalDateTime.now());
        Meeting updatedMeeting = meetingRepository.save(meeting);

        MeetingResponse response = new MeetingResponse(
                true,
                "Consulta encerrada com sucesso.",
                200,
                System.currentTimeMillis(),
                updatedMeeting);
        return ResponseEntity.ok(response);
    }

    // Método para validação do token (placeholder)
    private boolean isValidToken(String token) {
        // Implemente a lógica real de validação conforme sua documentação
        return true;
    }
}
