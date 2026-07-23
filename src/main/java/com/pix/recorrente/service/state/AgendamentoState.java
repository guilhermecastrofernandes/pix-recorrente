package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.model.Agendamento;

public interface AgendamentoState {
    void onEnter(Agendamento agendamento);
}
