package com.pix.recorrente.exception;

public sealed abstract class PixRecorrenteException extends RuntimeException
        permits AgendamentoDuplicadoException, AgendamentoNaoEncontradoException, AgendamentoRejeitadoFraudeException {

    protected PixRecorrenteException(String message) {
        super(message);
    }
}
