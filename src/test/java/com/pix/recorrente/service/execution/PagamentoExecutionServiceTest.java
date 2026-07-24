package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PagamentoExecutionServiceTest {
    private PagamentoExecutionService pagamentoExecutionService;

    @Mock
    private PagamentoRecorrenteRepository pagamentoRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        pagamentoExecutionService = new PagamentoExecutionService(pagamentoRepository);
    }

    @Test
    void testExecutarPagamento_Success() {
        PagamentoRecorrente pagamento = new PagamentoRecorrente();
        pagamento.setId(UUID.randomUUID());
        pagamento.setValor(new BigDecimal("50.00"));
        pagamento.setDataPrevista(LocalDate.now());
        pagamento.setStatus(EnumStatusPagamento.PENDENTE);

        when(pagamentoRepository.save(any())).thenReturn(pagamento);

        pagamentoExecutionService.executarPagamento(pagamento);

        assertEquals(EnumStatusPagamento.SUCESSO, pagamento.getStatus());
        assertNotNull(pagamento.getDataExecucao());
        verify(pagamentoRepository, times(2)).save(any());
    }

    @Test
    void testExecutarPagamento_Falha() {
        PagamentoRecorrente pagamento = new PagamentoRecorrente();
        pagamento.setId(UUID.randomUUID());
        pagamento.setValor(new BigDecimal("50.00"));
        pagamento.setDataPrevista(LocalDate.now());
        pagamento.setStatus(EnumStatusPagamento.PENDENTE);
        pagamento.setTentativas(0);

        when(pagamentoRepository.save(any())).thenThrow(new RuntimeException("Simulado"));

        assertThrows(RuntimeException.class, () ->
            pagamentoExecutionService.executarPagamento(pagamento)
        );

        assertEquals(EnumStatusPagamento.FALHA_PROCESSAMENTO, pagamento.getStatus());
        assertEquals(1, pagamento.getTentativas());
        verify(pagamentoRepository, times(2)).save(any());
    }

    @Test
    void testExecutarPagamento_MaxRetries() {
        PagamentoRecorrente pagamento = new PagamentoRecorrente();
        pagamento.setId(UUID.randomUUID());
        pagamento.setValor(new BigDecimal("50.00"));
        pagamento.setDataPrevista(LocalDate.now());
        pagamento.setStatus(EnumStatusPagamento.PENDENTE);
        pagamento.setTentativas(3);

        when(pagamentoRepository.save(any())).thenThrow(new RuntimeException("Simulado"));

        assertThrows(RuntimeException.class, () ->
            pagamentoExecutionService.executarPagamento(pagamento)
        );

        assertEquals(EnumStatusPagamento.ENVIADO_DLQ, pagamento.getStatus());
    }
}
