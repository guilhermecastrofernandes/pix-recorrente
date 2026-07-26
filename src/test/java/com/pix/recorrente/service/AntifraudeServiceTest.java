package com.pix.recorrente.service;

import com.pix.recorrente.config.AntifraudeProperties;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import com.pix.recorrente.domain.model.AnaliseFraude;
import com.pix.recorrente.service.fraud.FraudRule;
import com.pix.recorrente.service.fraud.RNF01ValueLimitRule;
import com.pix.recorrente.service.fraud.RNF02BlacklistRule;
import com.pix.recorrente.service.fraud.RNF03NocturneRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AntifraudeServiceTest {
    private static final String REGRA_RNF03 = "RNF-03: Agendamento em horário noturno com valor elevado";
    private static final ZoneId ZONA = ZoneId.systemDefault();

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
    }

    private AntifraudeService serviceNaHora(int hora) {
        Clock clock = Clock.fixed(
                ZonedDateTime.of(LocalDate.of(2026, 7, 26), java.time.LocalTime.of(hora, 0), ZONA).toInstant(),
                ZONA
        );

        List<FraudRule> fraudRules = List.of(
                new RNF01ValueLimitRule(properties),
                new RNF02BlacklistRule(properties),
                new RNF03NocturneRule(properties, clock)
        );
        return new AntifraudeService(fraudRules);
    }

    private AntifraudeService serviceDiurno() {
        return serviceNaHora(14);
    }

    @Test
    void testAnalyzeApproved_LowValueAndValidKey() {
        AnaliseFraude result = serviceDiurno().analisar("valido@banco.com.br", new BigDecimal("100.00"));

        assertEquals(EnumStatusRisco.APROVADO, result.statusRisco());
        assertEquals(100, result.score());
        assertTrue(result.regrasVioladas().isEmpty());
    }

    @Test
    void testAnalyzeManualReview_HighValue() {
        AnaliseFraude result = serviceDiurno().analisar("valido@banco.com.br", new BigDecimal("6000.00"));

        assertEquals(EnumStatusRisco.REVISAO_MANUAL, result.statusRisco());
        assertEquals(40, result.score());
        assertTrue(result.regrasVioladas().contains("RNF-01: Valor individual superior a R$ 5.000,00"));
    }

    @Test
    void testAnalyzeRejected_BlacklistedKey() {
        AnaliseFraude result = serviceDiurno().analisar("fraudulento@banco.com.br", new BigDecimal("100.00"));

        assertEquals(EnumStatusRisco.REJEITADO, result.statusRisco());
        assertEquals(10, result.score());
        assertTrue(result.regrasVioladas().contains("RNF-02: Chave Pix cadastrada em lista de risco nacional"));
    }

    @Test
    void testRNF03_NoiteComValorElevado_RevisaoManual() {
        AnaliseFraude result = serviceNaHora(22).analisar("valido@banco.com.br", new BigDecimal("2000.00"));

        assertEquals(EnumStatusRisco.REVISAO_MANUAL, result.statusRisco());
        assertEquals(40, result.score());
        assertTrue(result.regrasVioladas().contains(REGRA_RNF03));
    }

    @Test
    void testRNF03_MadrugadaComValorElevado_RevisaoManual() {
        AnaliseFraude result = serviceNaHora(3).analisar("valido@banco.com.br", new BigDecimal("2000.00"));

        assertEquals(EnumStatusRisco.REVISAO_MANUAL, result.statusRisco());
        assertTrue(result.regrasVioladas().contains(REGRA_RNF03));
    }

    @Test
    void testRNF03_ForaDaJanelaNoturna_Aprovado() {
        AnaliseFraude result = serviceNaHora(14).analisar("valido@banco.com.br", new BigDecimal("2000.00"));

        assertEquals(EnumStatusRisco.APROVADO, result.statusRisco());
        assertFalse(result.regrasVioladas().contains(REGRA_RNF03));
    }

    @Test
    void testRNF03_LimiteDaJanela_InicioInclusivoFimExclusivo() {
        assertTrue(serviceNaHora(20).analisar("valido@banco.com.br", new BigDecimal("2000.00"))
                .regrasVioladas().contains(REGRA_RNF03), "20h deve estar dentro da janela");

        assertFalse(serviceNaHora(6).analisar("valido@banco.com.br", new BigDecimal("2000.00"))
                .regrasVioladas().contains(REGRA_RNF03), "06h deve estar fora da janela");
    }

    @Test
    void testRNF03_NoiteComValorBaixo_Aprovado() {
        AnaliseFraude result = serviceNaHora(22).analisar("valido@banco.com.br", new BigDecimal("500.00"));

        assertEquals(EnumStatusRisco.APROVADO, result.statusRisco());
        assertTrue(result.regrasVioladas().isEmpty());
    }

    @Test
    void testRNF03_NaoRebaixaRejeitadoDaBlacklist() {
        AnaliseFraude result = serviceNaHora(22).analisar("fraudulento@banco.com.br", new BigDecimal("2000.00"));

        assertEquals(EnumStatusRisco.REJEITADO, result.statusRisco());
        assertEquals(10, result.score());
        assertTrue(result.regrasVioladas().contains(REGRA_RNF03));
    }

    @Test
    void testRNF03_JanelaConfiguravel_PermiteDemoEmQualquerHora() {
        properties.setHoraInicioNoturno(0);
        properties.setHoraFimNoturno(23);

        AnaliseFraude result = serviceNaHora(14).analisar("valido@banco.com.br", new BigDecimal("2000.00"));

        assertEquals(EnumStatusRisco.REVISAO_MANUAL, result.statusRisco());
        assertTrue(result.regrasVioladas().contains(REGRA_RNF03));
    }
}
