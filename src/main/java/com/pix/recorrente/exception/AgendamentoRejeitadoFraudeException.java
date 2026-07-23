package com.pix.recorrente.exception;

import com.pix.recorrente.domain.model.AnaliseFraude;

public final class AgendamentoRejeitadoFraudeException extends PixRecorrenteException {
    private final AnaliseFraude analiseFraude;

    public AgendamentoRejeitadoFraudeException(String message, AnaliseFraude analiseFraude) {
        super(message);
        this.analiseFraude = analiseFraude;
    }

    public AnaliseFraude getAnaliseFraude() {
        return analiseFraude;
    }
}
