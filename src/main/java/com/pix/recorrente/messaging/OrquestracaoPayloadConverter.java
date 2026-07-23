package com.pix.recorrente.messaging;

import com.pix.recorrente.exception.JsonSerializationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component
public class OrquestracaoPayloadConverter {
    public OrquestracaoPayload convert(Map<String, Object> rawPayload) {
        try {
            return new OrquestracaoPayload(
                UUID.fromString((String) rawPayload.get("id")),
                (String) rawPayload.get("clienteId"),
                new BigDecimal((String) rawPayload.get("valor")),
                LocalDate.parse((String) rawPayload.get("dataInicio")),
                (String) rawPayload.get("chavePixRecebedor"),
                (String) rawPayload.get("frequencia"),
                (Integer) rawPayload.get("quantidadeParcelas"),
                (String) rawPayload.get("chaveIdempotencia")
            );
        } catch (Exception e) {
            throw new JsonSerializationException("Falha ao converter payload de orquestração", e);
        }
    }
}
