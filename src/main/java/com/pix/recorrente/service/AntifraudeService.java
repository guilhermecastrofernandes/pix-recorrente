package com.pix.recorrente.service;

import com.pix.recorrente.domain.model.AnaliseFraude;
import com.pix.recorrente.service.fraud.FraudAnalysisContext;
import com.pix.recorrente.service.fraud.FraudRule;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AntifraudeService {
    private final List<FraudRule> fraudRules;

    public AntifraudeService(List<FraudRule> fraudRules) {
        this.fraudRules = fraudRules;
    }

    public AnaliseFraude analisar(String chavePixRecebedor, BigDecimal valor) {
        FraudAnalysisContext context = new FraudAnalysisContext();
        for (FraudRule rule : fraudRules) {
            rule.apply(context, chavePixRecebedor, valor);
        }
        return new AnaliseFraude(context.getStatusRisco(), context.getScore(), context.getRegrasVioladas(), LocalDateTime.now());
    }
}
