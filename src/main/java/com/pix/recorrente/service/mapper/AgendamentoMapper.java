package com.pix.recorrente.service.mapper;

import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.dto.AgendamentoRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AgendamentoMapper {
    public Agendamento toEntity(AgendamentoRequest request, String chaveIdempotencia, String analiseFraudeJson) {
        Agendamento agendamento = new Agendamento();
        agendamento.setClienteId(request.clienteId());
        agendamento.setChavePixRecebedor(request.chavePixRecebedor());
        agendamento.setValor(request.valor());
        agendamento.setFrequencia(request.frequencia());
        agendamento.setDataInicio(request.dataInicio());
        agendamento.setQuantidadeParcelas(request.quantidadeParcelas());
        agendamento.setDataCriacao(LocalDateTime.now());
        agendamento.setChaveIdempotencia(chaveIdempotencia);
        agendamento.setAnaliseFraudeJson(analiseFraudeJson);
        return agendamento;
    }
}
