package com.pix.recorrente.dto;

import com.pix.recorrente.domain.enums.EnumFrequencia;
import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AgendamentoResponse(
        UUID id,
        String clienteId,
        String chavePixRecebedor,
        BigDecimal valor,
        EnumFrequencia frequencia,
        LocalDate dataInicio,
        Integer quantidadeParcelas,
        EnumStatusAgendamento status,
        AnaliseFraudeResponse analiseFraude,
        LocalDateTime dataCriacao,
        List<PagamentoResponse> pagamentos
) {

    public record AnaliseFraudeResponse(
            EnumStatusRisco statusRisco,
            Integer score,
            List<String> regrasVioladas,
            LocalDateTime dataAnalise
    ) {}

    public record PagamentoResponse(
            UUID id,
            LocalDate dataPrevista,
            BigDecimal valor,
            EnumStatusPagamento status,
            LocalDateTime dataExecucao
    ) {}
}
