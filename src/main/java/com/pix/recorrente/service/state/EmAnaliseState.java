package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.model.Agendamento;
import org.springframework.stereotype.Component;

@Component
public class EmAnaliseState implements AgendamentoState {
    @Override
    public EnumStatusAgendamento status() {
        return EnumStatusAgendamento.EM_ANALISE;
    }

    @Override
    public void onEnter(Agendamento agendamento) {
    }
}
