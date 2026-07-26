package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.model.Agendamento;

public interface AgendamentoState {
    EnumStatusAgendamento status();

    void onEnter(Agendamento agendamento);
}
