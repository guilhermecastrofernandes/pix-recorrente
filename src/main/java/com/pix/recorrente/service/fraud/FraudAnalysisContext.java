package com.pix.recorrente.service.fraud;

import com.pix.recorrente.domain.enums.EnumStatusRisco;
import java.util.ArrayList;
import java.util.List;

public class FraudAnalysisContext {
    private EnumStatusRisco statusRisco = EnumStatusRisco.APROVADO;
    private int score = 5;
    private final List<String> regrasVioladas = new ArrayList<>();

    public EnumStatusRisco getStatusRisco() {
        return statusRisco;
    }

    public void setStatusRisco(EnumStatusRisco statusRisco) {
        this.statusRisco = statusRisco;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(this.score, score);
    }

    public List<String> getRegrasVioladas() {
        return regrasVioladas;
    }

    public void addRegraViolada(String regra) {
        regrasVioladas.add(regra);
    }
}
