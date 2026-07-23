package com.pix.recorrente.service.fraud;

import com.pix.recorrente.config.AntifraudeProperties;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class RNF03NocturneRule implements FraudRule {
    private final AntifraudeProperties properties;

    public RNF03NocturneRule(AntifraudeProperties properties) {
        this.properties = properties;
    }

    @Override
    public void apply(FraudAnalysisContext context, String chavePixRecebedor, BigDecimal valor) {
        int horaAtual = LocalDateTime.now().getHour();
        if ((horaAtual >= 20 || horaAtual < 6) && valor.compareTo(properties.getLimiteNoturno()) > 0) {
            context.addRegraViolada("RNF-03: Agendamento em horário noturno com valor elevado");
            if (context.getStatusRisco() != EnumStatusRisco.REJEITADO) {
                context.setStatusRisco(EnumStatusRisco.REVISAO_MANUAL);
                context.setScore(65);
            }
        }
    }
}
