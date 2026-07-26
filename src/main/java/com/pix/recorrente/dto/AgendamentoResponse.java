package com.pix.recorrente.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pix.recorrente.domain.enums.EnumFrequencia;
import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Estado do agendamento. Campos nulos são omitidos da serialização.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AgendamentoResponse(
        @Schema(example = "5e457ce1-1f52-42bc-8721-4c8998408a11")
        UUID id,

        @Schema(example = "12345678900")
        String clienteId,

        @Schema(example = "lojista@email.com")
        String chavePixRecebedor,

        @Schema(example = "150.00")
        BigDecimal valor,

        EnumFrequencia frequencia,

        @Schema(example = "2026-08-01")
        LocalDate dataInicio,

        @Schema(description = "Ausente quando a recorrência é ilimitada.", example = "12", nullable = true)
        Integer quantidadeParcelas,

        EnumStatusAgendamento status,

        AnaliseFraudeResponse analiseFraude,

        @Schema(description = "Instante do registro do agendamento.", example = "2026-07-26T14:00:48.640012")
        LocalDateTime dataCriacao,

        @Schema(description = """
                Parcelas geradas até o momento, em ordem de criação. Vazio para agendamentos
                `EM_ANALISE` ou `REJEITADO_FRAUDE`.
                """)
        List<PagamentoResponse> pagamentos
) {

    @Schema(description = "Resultado da avaliação de risco registrada na criação do agendamento.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AnaliseFraudeResponse(
            EnumStatusRisco statusRisco,

            @Schema(description = """
                    Escala **decrescente**: maior score significa menor risco.
                    `100` aprovado, `40` revisão manual, `10` rejeitado.
                    """, example = "100", minimum = "0", maximum = "100")
            Integer score,

            @Schema(description = "Identificador e descrição de cada regra acionada. Vazio quando aprovado.",
                    example = "[\"RNF-01: Valor individual superior a R$ 5.000,00\"]")
            List<String> regrasVioladas,

            @Schema(description = "Instante em que a análise de risco foi executada.",
                    example = "2026-07-26T14:00:48.641079")
            LocalDateTime dataAnalise
    ) {}

    @Schema(description = "Uma ocorrência (parcela) do agendamento.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PagamentoResponse(
            @Schema(example = "3f2a91c4-77bd-4a10-9f0e-1c2d3e4f5a6b")
            UUID id,

            @Schema(description = "Posição na recorrência, começando em 1.", example = "1", minimum = "1")
            Integer numeroParcela,

            @Schema(description = "Data a partir da qual a parcela é elegível à liquidação.", example = "2026-08-01")
            LocalDate dataPrevista,

            @Schema(example = "150.00")
            BigDecimal valor,

            EnumStatusPagamento status,

            @Schema(description = "Instante da liquidação. Ausente enquanto a parcela não liquidar com sucesso.",
                    example = "2026-08-01T09:15:22.110043", nullable = true)
            LocalDateTime dataExecucao
    ) {}
}
