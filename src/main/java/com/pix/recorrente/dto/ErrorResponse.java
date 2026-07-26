package com.pix.recorrente.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Formato único de erro da API. Campos nulos são omitidos.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "Código estável para tratamento programático.",
                example = "AGENDAMENTO_REJEITADO_FRAUDE",
                allowableValues = {"VALIDACAO_ERRO", "AGENDAMENTO_DUPLICADO",
                        "AGENDAMENTO_NAO_ENCONTRADO", "AGENDAMENTO_REJEITADO_FRAUDE"})
        String codigo,

        @Schema(description = "Descrição legível do erro.",
                example = "A solicitação de agendamento foi negada pelas regras de segurança.")
        String mensagem,

        @Schema(description = """
                Presente em `AGENDAMENTO_REJEITADO_FRAUDE` e, quando disponível,
                em `AGENDAMENTO_DUPLICADO`.
                """, nullable = true)
        AnaliseFraudeResponse analiseFraude
) {

    /**
     * Mesma forma de {@link AgendamentoResponse.AnaliseFraudeResponse}: o springdoc registra
     * os dois records sob o único schema `AnaliseFraudeResponse`, como no contrato de
     * referência. As anotações precisam ser idênticas nos dois lados — qual deles vence
     * a deduplicação não é determinístico.
     */
    @Schema(description = "Resultado da avaliação de risco registrada na criação do agendamento.")
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
}
