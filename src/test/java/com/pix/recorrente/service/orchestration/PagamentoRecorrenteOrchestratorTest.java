package com.pix.recorrente.service.orchestration;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.messaging.OrquestracaoPayload;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PagamentoRecorrenteOrchestratorTest {
    private PagamentoRecorrenteOrchestrator orchestrator;

    @Mock
    private PagamentoRecorrenteRepository pagamentoRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        orchestrator = new PagamentoRecorrenteOrchestrator(pagamentoRepository);
    }

    @Test
    void testCriarOuIgnorar_Create() {
        UUID agendamentoId = UUID.randomUUID();
        OrquestracaoPayload payload = new OrquestracaoPayload(
            agendamentoId,
            "cliente1",
            new BigDecimal("50.00"),
            LocalDate.now(),
            "key-receiver@pix",
            "MENSAL",
            12,
            "key-1"
        );

        when(pagamentoRepository.findByChaveIdempotencia("key-1")).thenReturn(Optional.empty());
        when(pagamentoRepository.save(any())).thenReturn(new PagamentoRecorrente());

        orchestrator.criarOuIgnorar(payload);

        verify(pagamentoRepository).save(any());
    }

    @Test
    void testCriarOuIgnorar_Ignorar() {
        UUID agendamentoId = UUID.randomUUID();
        OrquestracaoPayload payload = new OrquestracaoPayload(
            agendamentoId,
            "cliente1",
            new BigDecimal("50.00"),
            LocalDate.now(),
            "key-receiver@pix",
            "MENSAL",
            12,
            "key-1"
        );

        PagamentoRecorrente existente = new PagamentoRecorrente();
        when(pagamentoRepository.findByChaveIdempotencia("key-1")).thenReturn(Optional.of(existente));

        orchestrator.criarOuIgnorar(payload);

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void testCriarOuIgnorar_StatusPendente() {
        UUID agendamentoId = UUID.randomUUID();
        OrquestracaoPayload payload = new OrquestracaoPayload(
            agendamentoId,
            "cliente1",
            new BigDecimal("50.00"),
            LocalDate.of(2026, 7, 23),
            "key-receiver@pix",
            "MENSAL",
            12,
            "key-1"
        );

        when(pagamentoRepository.findByChaveIdempotencia("key-1")).thenReturn(Optional.empty());

        PagamentoRecorrente capturado = new PagamentoRecorrente();
        when(pagamentoRepository.save(any())).thenAnswer(invocation -> {
            capturado.setId(UUID.randomUUID());
            PagamentoRecorrente arg = invocation.getArgument(0);
            capturado.setStatus(arg.getStatus());
            capturado.setValor(arg.getValor());
            capturado.setDataPrevista(arg.getDataPrevista());
            return capturado;
        });

        orchestrator.criarOuIgnorar(payload);

        assertEquals(EnumStatusPagamento.PENDENTE, capturado.getStatus());
        assertEquals(new BigDecimal("50.00"), capturado.getValor());
    }
}
