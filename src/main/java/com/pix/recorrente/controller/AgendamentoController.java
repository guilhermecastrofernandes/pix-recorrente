package com.pix.recorrente.controller;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.dto.AgendamentoRequest;
import com.pix.recorrente.dto.AgendamentoResponse;
import com.pix.recorrente.dto.ErrorResponse;
import com.pix.recorrente.service.AgendamentoService;
import com.pix.recorrente.service.builder.AgendamentoResponseBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Agendamentos", description = "Registro e consulta de agendamentos de Pix recorrente.")
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

    @Operation(
            operationId = "criarAgendamento",
            summary = "Registra um agendamento de Pix recorrente",
            description = """
                    Submete a solicitação ao módulo antifraude e, se aprovada, registra o agendamento
                    e publica o evento de orquestração que dará origem à primeira parcela.

                    A operação é idempotente pelo header `X-Idempotency-Key`.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agendamento aprovado e ativado.",
                    content = @Content(schema = @Schema(implementation = AgendamentoResponse.class))),
            @ApiResponse(responseCode = "202",
                    description = "Aceito para revisão manual. O agendamento fica `EM_ANALISE` e nenhuma parcela é gerada.",
                    content = @Content(schema = @Schema(implementation = AgendamentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload inválido (`VALIDACAO_ERRO`).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Chave de idempotência já utilizada (`AGENDAMENTO_DUPLICADO`).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Rejeitado pelo antifraude (`AGENDAMENTO_REJEITADO_FRAUDE`).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AgendamentoResponse> criarAgendamento(
            @Valid @RequestBody AgendamentoRequest request,
            @Parameter(
                    description = """
                            Chave de idempotência definida pelo cliente. Repetir a mesma chave devolve `409`
                            em vez de criar um novo agendamento. Também dá origem à chave de cada parcela,
                            no formato `{chave}-{numeroParcela}`.
                            """,
                    required = true,
                    example = "pedido-2026-08-001")
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {

        Agendamento agendamento = agendamentoService.criarAgendamento(request, idempotencyKey);
        AgendamentoResponse response = responseBuilder.build(agendamento);
        HttpStatus statusCode = mapStatusToHttpStatus(agendamento.getStatus());

        return ResponseEntity.status(statusCode).body(response);
    }

    @Operation(
            operationId = "obterAgendamento",
            summary = "Consulta um agendamento e suas parcelas",
            description = "Devolve o estado atual do agendamento, incluindo a análise de fraude e as parcelas geradas até o momento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agendamento localizado.",
                    content = @Content(schema = @Schema(implementation = AgendamentoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum agendamento para o ID informado (`AGENDAMENTO_NAO_ENCONTRADO`).",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoResponse> obterAgendamento(
            @Parameter(description = "Identificador do agendamento.",
                    example = "5e457ce1-1f52-42bc-8721-4c8998408a11")
            @PathVariable UUID id) {
        Agendamento agendamento = agendamentoService.obterAgendamentoPorId(id);
        AgendamentoResponse response = responseBuilder.build(agendamento);
        return ResponseEntity.ok(response);
    }

    private HttpStatus mapStatusToHttpStatus(EnumStatusAgendamento status) {
        return status == EnumStatusAgendamento.EM_ANALISE ? HttpStatus.ACCEPTED : HttpStatus.CREATED;
    }
}
