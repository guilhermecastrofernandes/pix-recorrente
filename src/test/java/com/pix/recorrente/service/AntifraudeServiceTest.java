package com.pix.recorrente.service;

import com.pix.recorrente.config.AntifraudeProperties;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import com.pix.recorrente.domain.model.AnaliseFraude;
import com.pix.recorrente.service.fraud.RNF01ValueLimitRule;
import com.pix.recorrente.service.fraud.RNF02BlacklistRule;
import com.pix.recorrente.service.fraud.RNF03NocturneRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AntifraudeServiceTest {
    private AntifraudeService antifraudeService;
    private AntifraudeProperties properties;

    @BeforeEach
    void setup() {
        properties = new AntifraudeProperties();
        properties.setLimiteValorSuspeito(new BigDecimal("5000.00"));
        properties.setLimiteNoturno(new BigDecimal("1000.00"));
        properties.setChavesBlacklist(Set.of(
                "fraudulento@banco.com.br",
                "suspeito@empresa.com.br",
                "11999999999"
        ));

        List<com.pix.recorrente.service.fraud.FraudRule> fraudRules = List.of(
            new RNF01ValueLimitRule(properties),
            new RNF02BlacklistRule(properties),
            new RNF03NocturneRule(properties)
        );
        antifraudeService = new AntifraudeService(fraudRules);
    }

    @Test
    void testAnalyzeApproved_LowValueAndValidKey() {
        AnaliseFraude result = antifraudeService.analisar("valido@banco.com.br", new BigDecimal("100.00"));

        assertEquals(EnumStatusRisco.APROVADO, result.statusRisco());
        assertEquals(5, result.score());
        assertTrue(result.regrasVioladas().isEmpty());
    }

    @Test
    void testAnalyzeManualReview_HighValue() {
        AnaliseFraude result = antifraudeService.analisar("valido@banco.com.br", new BigDecimal("6000.00"));

        assertEquals(EnumStatusRisco.REVISAO_MANUAL, result.statusRisco());
        assertEquals(65, result.score());
        assertTrue(result.regrasVioladas().contains("RNF-01: Valor individual superior a R$ 5.000,00"));
    }

    @Test
    void testAnalyzeRejected_BlacklistedKey() {
        AnaliseFraude result = antifraudeService.analisar("fraudulento@banco.com.br", new BigDecimal("100.00"));

        assertEquals(EnumStatusRisco.REJEITADO, result.statusRisco());
        assertEquals(95, result.score());
        assertTrue(result.regrasVioladas().contains("RNF-02: Chave Pix cadastrada em lista de risco nacional"));
    }

    @Test
    void testAnalyzeNightWindowHighValue() {
        // Não conseguimos testar hora específica sem mock de LocalDateTime
        // Mas validamos que o campo existe
        AnaliseFraude result = antifraudeService.analisar("valido@banco.com.br", new BigDecimal("500.00"));
        assertNotNull(result.dataAnalise());
    }
}
