package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.messaging.OrquestracaoPublisher;
import org.springframework.stereotype.Component;

@Component
public class AtivoState implements AgendamentoState {
    private final OrquestracaoPublisher orquestracaoPublisher;

    public AtivoState(OrquestracaoPublisher orquestracaoPublisher) {
        this.orquestracaoPublisher = orquestracaoPublisher;
    }

    @Override
    public void onEnter(Agendamento agendamento) {
        orquestracaoPublisher.publicarAgendamento(agendamento);
    }
}
