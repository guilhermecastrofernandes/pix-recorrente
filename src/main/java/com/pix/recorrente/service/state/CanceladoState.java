package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.model.Agendamento;
import org.springframework.stereotype.Component;

@Component
public class CanceladoState implements AgendamentoState {
    @Override
    public EnumStatusAgendamento status() {
        return EnumStatusAgendamento.CANCELADO;
    }

    @Override
    public void onEnter(Agendamento agendamento) {
    }
}
