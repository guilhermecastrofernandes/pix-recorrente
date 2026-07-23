package com.pix.recorrente.service.fraud;

import com.pix.recorrente.config.AntifraudeProperties;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class RNF02BlacklistRule implements FraudRule {
    private final AntifraudeProperties properties;

    public RNF02BlacklistRule(AntifraudeProperties properties) {
        this.properties = properties;
    }

    @Override
    public void apply(FraudAnalysisContext context, String chavePixRecebedor, BigDecimal valor) {
        if (properties.getChavesBlacklist().contains(chavePixRecebedor)) {
            context.addRegraViolada("RNF-02: Chave Pix cadastrada em lista de risco nacional");
            context.setStatusRisco(EnumStatusRisco.REJEITADO);
            context.setScore(95);
        }
    }
}
