package com.pix.recorrente.service.fraud;

import java.util.ArrayList;
import java.util.List;

public class FraudAnalysisContext {
    private int score = 100;
    private final List<String> regrasVioladas = new ArrayList<>();

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.min(this.score, score);
    }

    public List<String> getRegrasVioladas() {
        return regrasVioladas;
    }

    public void addRegraViolada(String regra) {
        regrasVioladas.add(regra);
    }
}
