package com.pix.recorrente.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String codigo,
        String mensagem,
        AnaliseFraudeResponse analiseFraude
) {

    public record AnaliseFraudeResponse(
            EnumStatusRisco statusRisco,
            Integer score,
            List<String> regrasVioladas,
            LocalDateTime dataAnalise
    ) {}
}
