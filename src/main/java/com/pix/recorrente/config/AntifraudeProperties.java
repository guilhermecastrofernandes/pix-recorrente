package com.pix.recorrente.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@Data
@ConfigurationProperties(prefix = "antifraude")
public class AntifraudeProperties {

    private BigDecimal limiteValorSuspeito;
    private BigDecimal limiteNoturno;
    private Set<String> chavesBlacklist;
}
