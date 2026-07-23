package com.pix.recorrente.messaging;

import com.pix.recorrente.domain.model.Agendamento;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrquestracaoPayloadBuilder {
    public Map<String, Object> build(Agendamento agendamento) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", agendamento.getId().toString());
        payload.put("clienteId", agendamento.getClienteId());
        payload.put("chavePixRecebedor", agendamento.getChavePixRecebedor());
        payload.put("valor", agendamento.getValor().toString());
        payload.put("frequencia", agendamento.getFrequencia().toString());
        payload.put("dataInicio", agendamento.getDataInicio().toString());
        payload.put("quantidadeParcelas", agendamento.getQuantidadeParcelas());
        payload.put("chaveIdempotencia", agendamento.getChaveIdempotencia());
        return payload;
    }
}
