package com.pix.recorrente.service.mapper;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.dto.AgendamentoRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AgendamentoMapper {
    public Agendamento toEntity(AgendamentoRequest request, String chaveIdempotencia, String analiseFraudeJson) {
        return Agendamento.builder()
                .clienteId(request.clienteId())
                .chavePixRecebedor(request.chavePixRecebedor())
                .valor(request.valor())
                .frequencia(request.frequencia())
                .dataInicio(request.dataInicio())
                .quantidadeParcelas(request.quantidadeParcelas())
                .dataCriacao(LocalDateTime.now())
                .chaveIdempotencia(chaveIdempotencia)
                .analiseFraudeJson(analiseFraudeJson)
                .status(EnumStatusAgendamento.EM_ANALISE)
                .build();
    }
}
