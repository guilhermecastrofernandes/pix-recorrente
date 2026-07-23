package com.pix.recorrente.service.fraud;

import com.pix.recorrente.config.AntifraudeProperties;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class RNF01ValueLimitRule implements FraudRule {
    private final AntifraudeProperties properties;

    public RNF01ValueLimitRule(AntifraudeProperties properties) {
        this.properties = properties;
    }

    @Override
    public void apply(FraudAnalysisContext context, String chavePixRecebedor, BigDecimal valor) {
        if (valor.compareTo(properties.getLimiteValorSuspeito()) > 0) {
            context.addRegraViolada("RNF-01: Valor individual superior a R$ 5.000,00");
            context.setStatusRisco(EnumStatusRisco.REVISAO_MANUAL);
            context.setScore(60);
        }
    }
}
