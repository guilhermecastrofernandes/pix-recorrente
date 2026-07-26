package com.pix.recorrente.service.execution;


public final class ChaveParcelaFactory {
    private ChaveParcelaFactory() {
    }

    public static String chaveDaParcela(String chaveAgendamento, int numeroParcela) {
        return chaveAgendamento + "-" + numeroParcela;
    }
}
