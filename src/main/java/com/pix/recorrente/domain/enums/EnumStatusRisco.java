package com.pix.recorrente.domain.enums;

public enum EnumStatusRisco {
    APROVADO, REJEITADO, REVISAO_MANUAL;

    /**
     * Escala decrescente: maior score = menor risco.
     * <=10 rejeitado, 11-40 revisão manual, 41-100 aprovado.
     */
    public static EnumStatusRisco fromScore(int score) {
        if (score <= 10) {
            return REJEITADO;
        }
        if (score <= 40) {
            return REVISAO_MANUAL;
        }
        return APROVADO;
    }
}
