package com.pix.recorrente.controller;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.dto.AgendamentoRequest;
import com.pix.recorrente.dto.AgendamentoResponse;
import com.pix.recorrente.service.AgendamentoService;
import com.pix.recorrente.service.builder.AgendamentoResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/agendamentos")
public class AgendamentoController {
    private final AgendamentoService agendamentoService;
    private final AgendamentoResponseBuilder responseBuilder;

    public AgendamentoController(AgendamentoService agendamentoService,
                                 AgendamentoResponseBuilder responseBuilder) {
        this.agendamentoService = agendamentoService;
        this.responseBuilder = responseBuilder;
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponse> criarAgendamento(
            @Valid @RequestBody AgendamentoRequest request,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {

        Agendamento agendamento = agendamentoService.criarAgendamento(request, idempotencyKey);
        AgendamentoResponse response = responseBuilder.build(agendamento);
        HttpStatus statusCode = mapStatusToHttpStatus(agendamento.getStatus());

        return ResponseEntity.status(statusCode).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> obterAgendamento(@PathVariable UUID id) {
        Agendamento agendamento = agendamentoService.obterAgendamentoPorId(id);
        AgendamentoResponse response = responseBuilder.build(agendamento);
        return ResponseEntity.ok(response);
    }

    private HttpStatus mapStatusToHttpStatus(EnumStatusAgendamento status) {
        return status == EnumStatusAgendamento.EM_ANALISE ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
    }
}
