package com.pix.recorrente.service.mapper;

import com.pix.recorrente.domain.enums.EnumFrequencia;
import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.dto.AgendamentoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AgendamentoMapperTest {

    private AgendamentoMapper mapper;
    private AgendamentoRequest validRequest;

    @BeforeEach
    void setup() {
        mapper = new AgendamentoMapper();
        validRequest = new AgendamentoRequest(
                "cliente-123",
                "usuario@email.com",
                new BigDecimal("1000.00"),
                EnumFrequencia.MENSAL,
                LocalDate.of(2026, 8, 1),
                12
        );
    }

    @Test
    void testToEntity_AllFieldsMapped_Success() {
        String chaveIdempotencia = "idem-key-123";
        String analiseFraudeJson = "{\"statusRisco\":\"APROVADO\"}";

        Agendamento result = mapper.toEntity(validRequest, chaveIdempotencia, analiseFraudeJson);

        assertNotNull(result);
        assertEquals("cliente-123", result.getClienteId());
        assertEquals("usuario@email.com", result.getChavePixRecebedor());
        assertEquals(new BigDecimal("1000.00"), result.getValor());
        assertEquals(EnumFrequencia.MENSAL, result.getFrequencia());
        assertEquals(LocalDate.of(2026, 8, 1), result.getDataInicio());
        assertEquals(12, result.getQuantidadeParcelas());
    }

    @Test
    void testToEntity_InitialStatusIsEmAnalise() {
        String chaveIdempotencia = "idem-key-123";
        String analiseFraudeJson = "{}";

        Agendamento result = mapper.toEntity(validRequest, chaveIdempotencia, analiseFraudeJson);

        assertEquals(EnumStatusAgendamento.EM_ANALISE, result.getStatus());
    }

    @Test
    void testToEntity_ChaveIdempotenciaStored() {
        String chaveIdempotencia = "idem-key-unique";
        String analiseFraudeJson = "{}";

        Agendamento result = mapper.toEntity(validRequest, chaveIdempotencia, analiseFraudeJson);

        assertEquals("idem-key-unique", result.getChaveIdempotencia());
    }

    @Test
    void testToEntity_AnaliseFraudeJsonStored() {
        String chaveIdempotencia = "idem-key";
        String analiseFraudeJson = "{\"statusRisco\":\"REJEITADO\",\"score\":10}";

        Agendamento result = mapper.toEntity(validRequest, chaveIdempotencia, analiseFraudeJson);

        assertEquals(analiseFraudeJson, result.getAnaliseFraudeJson());
    }

    @Test
    void testToEntity_DataCriacaoSetToNow() {
        String chaveIdempotencia = "idem-key";
        String analiseFraudeJson = "{}";
        LocalDateTime beforeMapping = LocalDateTime.now().minusSeconds(1);

        Agendamento result = mapper.toEntity(validRequest, chaveIdempotencia, analiseFraudeJson);

        LocalDateTime afterMapping = LocalDateTime.now().plusSeconds(1);
        assertNotNull(result.getDataCriacao());
        assertTrue(result.getDataCriacao().isAfter(beforeMapping));
        assertTrue(result.getDataCriacao().isBefore(afterMapping));
    }

    @Test
    void testToEntity_NoIdAssigned() {
        String chaveIdempotencia = "idem-key";
        String analiseFraudeJson = "{}";

        Agendamento result = mapper.toEntity(validRequest, chaveIdempotencia, analiseFraudeJson);

        assertNull(result.getId());
    }

    @Test
    void testToEntity_DifferentFrequencies() {
        AgendamentoRequest monthlyRequest = new AgendamentoRequest(
                "cliente",
                "email@test.com",
                new BigDecimal("500.00"),
                EnumFrequencia.MENSAL,
                LocalDate.now().plusDays(1),
                6
        );

        Agendamento result = mapper.toEntity(monthlyRequest, "idem", "{}");

        assertEquals(EnumFrequencia.MENSAL, result.getFrequencia());
    }

    @Test
    void testToEntity_VariousAmounts() {
        AgendamentoRequest largeAmountRequest = new AgendamentoRequest(
                "cliente",
                "email@test.com",
                new BigDecimal("10000.00"),
                EnumFrequencia.MENSAL,
                LocalDate.now().plusDays(1),
                24
        );

        Agendamento result = mapper.toEntity(largeAmountRequest, "idem", "{}");

        assertEquals(new BigDecimal("10000.00"), result.getValor());
    }
}
