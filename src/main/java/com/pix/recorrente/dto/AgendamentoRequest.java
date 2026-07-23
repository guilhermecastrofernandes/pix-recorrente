package com.pix.recorrente.dto;

import com.pix.recorrente.domain.enums.EnumFrequencia;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AgendamentoRequest(
        @NotBlank(message = "clienteId é obrigatório")
        String clienteId,

        @NotBlank(message = "chavePixRecebedor é obrigatória")
        String chavePixRecebedor,

        @NotNull(message = "valor é obrigatório")
        @DecimalMin(value = "0.01", message = "valor deve ser maior que zero")
        @Digits(integer = 19, fraction = 2, message = "valor deve ter no máximo 2 casas decimais")
        BigDecimal valor,

        @NotNull(message = "frequencia é obrigatória")
        EnumFrequencia frequencia,

        @NotNull(message = "dataInicio é obrigatória")
        LocalDate dataInicio,

        Integer quantidadeParcelas
) {}
