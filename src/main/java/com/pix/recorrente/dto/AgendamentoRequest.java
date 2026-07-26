package com.pix.recorrente.dto;

import com.pix.recorrente.domain.enums.EnumFrequencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Solicitação de registro de um agendamento recorrente.")
public record AgendamentoRequest(
        @Schema(description = "Identificador do cliente pagador.", example = "12345678900")
        @NotBlank(message = "clienteId é obrigatório")
        String clienteId,

        @Schema(description = "Chave Pix do recebedor. Avaliada contra a lista de risco (RNF-02).",
                example = "lojista@email.com")
        @NotBlank(message = "chavePixRecebedor é obrigatória")
        String chavePixRecebedor,

        @Schema(description = """
                Valor de cada parcela, com no máximo 2 casas decimais.
                Acima de R$ 5.000,00 dispara revisão manual (RNF-01).
                """, example = "150.00")
        @NotNull(message = "valor é obrigatório")
        @DecimalMin(value = "0.01", message = "valor deve ser maior que zero")
        @Digits(integer = 19, fraction = 2, message = "valor deve ter no máximo 2 casas decimais")
        BigDecimal valor,

        @NotNull(message = "frequencia é obrigatória")
        EnumFrequencia frequencia,

        @Schema(description = """
                Data prevista da primeira parcela. Datas passadas são aceitas — a parcela
                nasce vencida e é liquidada no ciclo seguinte do agendador.
                """, example = "2026-08-01")
        @NotNull(message = "dataInicio é obrigatória")
        LocalDate dataInicio,

        @Schema(description = """
                Total de parcelas. Ausente ou `null` significa **recorrência ilimitada**:
                as parcelas seguem sendo encadeadas e o agendamento nunca vira `CONCLUIDO`.
                """, example = "12", nullable = true)
        Integer quantidadeParcelas
) {}
