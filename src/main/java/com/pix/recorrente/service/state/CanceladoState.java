package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.model.Agendamento;
import org.springframework.stereotype.Component;

@Component
public class CanceladoState implements AgendamentoState {
    @Override
    public void onEnter(Agendamento agendamento) {
        // Não fazer nada para estado cancelado
    }
}
