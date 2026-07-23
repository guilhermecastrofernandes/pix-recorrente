package com.pix.recorrente.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrquestracaoPayload(
    UUID agendamentoId,
    String clienteId,
    BigDecimal valor,
    LocalDate dataInicio,
    String chavePixRecebedor,
    String frequencia,
    Integer quantidadeParcelas,
    String chaveIdempotencia
) {}
