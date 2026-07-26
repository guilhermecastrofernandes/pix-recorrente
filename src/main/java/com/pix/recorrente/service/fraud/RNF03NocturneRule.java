package com.pix.recorrente.service.fraud;

import com.pix.recorrente.config.AntifraudeProperties;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class RNF03NocturneRule implements FraudRule {
    private final AntifraudeProperties properties;
    private final Clock clock;

    public RNF03NocturneRule(AntifraudeProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void apply(FraudAnalysisContext context, String chavePixRecebedor, BigDecimal valor) {
        int horaAtual = LocalDateTime.now(clock).getHour();

        if (dentroDaJanelaNoturna(horaAtual) && valor.compareTo(properties.getLimiteNoturno()) > 0) {
            context.addRegraViolada("RNF-03: Agendamento em horário noturno com valor elevado");

            if (context.getStatusRisco() != EnumStatusRisco.REJEITADO) {
                context.setStatusRisco(EnumStatusRisco.REVISAO_MANUAL);
                context.setScore(40);
            }
        }
    }

    private boolean dentroDaJanelaNoturna(int hora) {
        int inicio = properties.getHoraInicioNoturno();
        int fim = properties.getHoraFimNoturno();

        return inicio > fim
                ? hora >= inicio || hora < fim
                : hora >= inicio && hora < fim;
    }
}
