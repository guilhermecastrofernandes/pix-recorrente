package com.pix.recorrente.exception;

import com.pix.recorrente.domain.model.Agendamento;

public final class AgendamentoDuplicadoException extends PixRecorrenteException {
    private final Agendamento agendamento;

    public AgendamentoDuplicadoException(String message, Agendamento agendamento) {
        super(message);
        this.agendamento = agendamento;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }
}
