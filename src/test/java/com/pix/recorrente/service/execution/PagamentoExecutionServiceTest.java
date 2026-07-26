package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoExecutionServiceTest {

    @Mock
    private PagamentoRecorrenteRepository pagamentoRepository;

    @Mock
    private PixGatewaySimulator pixGateway;

    @Mock
    private ProximaParcelaService proximaParcelaService;

    @InjectMocks
    private PagamentoExecutionService pagamentoExecutionService;

    private PagamentoRecorrente mockPagamento;

    @BeforeEach
    void setup() {
        mockPagamento = PagamentoRecorrente.builder()
                .id(UUID.randomUUID())
                .agendamentoId(UUID.randomUUID())
                .valor(new BigDecimal("150.00"))
                .dataPrevista(LocalDate.now())
                .status(EnumStatusPagamento.PENDENTE)
                .chaveIdempotencia("idem-key-pagamento-1")
                .numeroParcela(1)
                .tentativas(0)
                .build();
    }

    private void gatewayFalha(String mensagem) {
        doThrow(new PixGatewayException(mensagem)).when(pixGateway).liquidar(mockPagamento);
    }

    @Test
    void testExecutarPagamento_Success_UpdatesStatusToSucesso() {
        pagamentoExecutionService.executarPagamento(mockPagamento);

        assertEquals(EnumStatusPagamento.SUCESSO, mockPagamento.getStatus());
        assertNotNull(mockPagamento.getDataExecucao());
        assertNull(mockPagamento.getMensagemErro());
        verify(pagamentoRepository, times(2)).save(mockPagamento);
    }

    @Test
    void testExecutarPagamento_Success_PersistsDataExecucao() {
        LocalDateTime antes = LocalDateTime.now();
        pagamentoExecutionService.executarPagamento(mockPagamento);
        LocalDateTime depois = LocalDateTime.now();

        assertNotNull(mockPagamento.getDataExecucao());
        assertTrue(mockPagamento.getDataExecucao().isAfter(antes.minusSeconds(1)));
        assertTrue(mockPagamento.getDataExecucao().isBefore(depois.plusSeconds(1)));
    }

    @Test
    void testExecutarPagamento_Success_EncadeiaProximaParcela() {
        pagamentoExecutionService.executarPagamento(mockPagamento);

        verify(proximaParcelaService).agendarProxima(mockPagamento);
    }

    @Test
    void testExecutarPagamento_ClearsErrorMessage_OnSuccess() {
        mockPagamento.setMensagemErro("Erro anterior");

        pagamentoExecutionService.executarPagamento(mockPagamento);

        assertNull(mockPagamento.getMensagemErro());
    }

    @Test
    void testExecutarPagamento_Failure_UpdatesStatusToFalha() {
        gatewayFalha("Simulação de erro");

        pagamentoExecutionService.executarPagamento(mockPagamento);

        assertEquals(EnumStatusPagamento.FALHA_PROCESSAMENTO, mockPagamento.getStatus());
        assertNotNull(mockPagamento.getMensagemErro());
        assertTrue(mockPagamento.getMensagemErro().contains("Simulação"));
    }

    @Test
    void testExecutarPagamento_Failure_NaoRelancaEPersisteEstadoDeRetry() {
        gatewayFalha("Erro");

        assertDoesNotThrow(() -> pagamentoExecutionService.executarPagamento(mockPagamento));

        assertEquals(1, mockPagamento.getTentativas());
        assertNotNull(mockPagamento.getProximaExecucao());
        verify(pagamentoRepository, times(2)).save(mockPagamento);
    }

    @Test
    void testExecutarPagamento_Failure_NaoEncadeiaProximaParcela() {
        gatewayFalha("Erro");

        pagamentoExecutionService.executarPagamento(mockPagamento);

        verify(proximaParcelaService, never()).agendarProxima(any());
    }

    @Test
    void testExecutarPagamento_BelowMaxRetries_FalhaProcessamento() {
        mockPagamento.setTentativas(1);
        gatewayFalha("Erro");

        pagamentoExecutionService.executarPagamento(mockPagamento);

        assertEquals(EnumStatusPagamento.FALHA_PROCESSAMENTO, mockPagamento.getStatus());
        assertEquals(2, mockPagamento.getTentativas());
    }

    @Test
    void testExecutarPagamento_MaxRetriesExceeded_SendsToDLQ() {
        mockPagamento.setTentativas(2);
        gatewayFalha("Erro");

        pagamentoExecutionService.executarPagamento(mockPagamento);

        assertEquals(3, mockPagamento.getTentativas());
        assertEquals(EnumStatusPagamento.ENVIADO_DLQ, mockPagamento.getStatus());
    }

    @Test
    void testExecutarPagamento_Failure_BackoffExponencialCresce() {
        gatewayFalha("Erro");

        pagamentoExecutionService.executarPagamento(mockPagamento);
        LocalDateTime aposPrimeira = mockPagamento.getProximaExecucao();

        pagamentoExecutionService.executarPagamento(mockPagamento);
        LocalDateTime aposSegunda = mockPagamento.getProximaExecucao();

        assertTrue(aposSegunda.isAfter(aposPrimeira));
    }
}
