package com.pix.recorrente.service;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.domain.model.AnaliseFraude;
import com.pix.recorrente.dto.AgendamentoRequest;
import com.pix.recorrente.exception.AgendamentoDuplicadoException;
import com.pix.recorrente.exception.AgendamentoRejeitadoFraudeException;
import com.pix.recorrente.repository.AgendamentoRepository;
import com.pix.recorrente.service.mapper.AgendamentoMapper;
import com.pix.recorrente.service.serialization.AnaliseFraudeJsonSerializer;
import com.pix.recorrente.service.state.AgendamentoStatusTransitioner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgendamentoServiceTest {
    private AgendamentoService agendamentoService;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AntifraudeService antifraudeService;

    @Mock
    private AgendamentoStatusTransitioner statusTransitioner;

    @Mock
    private AgendamentoMapper agendamentoMapper;

    @Mock
    private AnaliseFraudeJsonSerializer analiseFraudeJsonSerializer;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        agendamentoService = new AgendamentoService(
            agendamentoRepository,
            antifraudeService,
            statusTransitioner,
            agendamentoMapper,
            analiseFraudeJsonSerializer
        );

        AnaliseFraude analiseFraudeAproved = new AnaliseFraude(
            EnumStatusRisco.APROVADO,
            5,
            List.of(),
            LocalDateTime.now()
        );
        when(antifraudeService.analisar(any(), any())).thenReturn(analiseFraudeAproved);
        when(analiseFraudeJsonSerializer.serialize(any())).thenReturn("{}");
    }

    @Test
    void testCriarAgendamento_Success() {
        AgendamentoRequest request = new AgendamentoRequest(
            "12345678900",
            "user@email.com",
            new BigDecimal("150.00"),
            null,
            LocalDate.of(2026, 8, 1),
            12
        );

        Agendamento agendamento = new Agendamento();
        agendamento.setId(java.util.UUID.randomUUID());
        agendamento.setStatus(EnumStatusAgendamento.EM_ANALISE);

        when(agendamentoRepository.findByChaveIdempotencia("key-1")).thenReturn(Optional.empty());
        when(agendamentoMapper.toEntity(any(), any(), any())).thenReturn(agendamento);
        when(agendamentoRepository.saveAndFlush(any())).thenReturn(agendamento);

        Agendamento result = agendamentoService.criarAgendamento(request, "key-1");

        assertNotNull(result);
        verify(agendamentoRepository).saveAndFlush(any());
    }

    @Test
    void testCriarAgendamento_Duplicado() {
        AgendamentoRequest request = new AgendamentoRequest(
            "12345678900",
            "user@email.com",
            new BigDecimal("150.00"),
            null,
            LocalDate.of(2026, 8, 1),
            12
        );

        Agendamento existente = new Agendamento();
        when(agendamentoRepository.findByChaveIdempotencia("key-1")).thenReturn(Optional.of(existente));

        assertThrows(AgendamentoDuplicadoException.class, () ->
            agendamentoService.criarAgendamento(request, "key-1")
        );
    }

    @Test
    void testObterAgendamento() {
        Agendamento agendamento = new Agendamento();
        when(agendamentoRepository.findById(any())).thenReturn(Optional.of(agendamento));

        Agendamento result = agendamentoService.obterAgendamentoPorId(java.util.UUID.randomUUID());

        assertNotNull(result);
    }
}
