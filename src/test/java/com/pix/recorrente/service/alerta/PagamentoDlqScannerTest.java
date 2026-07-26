package com.pix.recorrente.service.alerta;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoDlqScannerTest {

    private static final ZoneId ZONA = ZoneId.systemDefault();
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 7, 26, 15, 0);

    @Mock
    private PagamentoRecorrenteRepository pagamentoRepository;

    @Mock
    private AlertaPort alertaPort;

    private PagamentoDlqScanner scanner;

    @BeforeEach
    void setup() {
        Clock clock = Clock.fixed(ZonedDateTime.of(AGORA, ZONA).toInstant(), ZONA);
        scanner = new PagamentoDlqScanner(pagamentoRepository, alertaPort, clock);
    }

    private PagamentoRecorrente pagamentoEmDlq() {
        return PagamentoRecorrente.builder()
                .id(UUID.randomUUID())
                .agendamentoId(UUID.randomUUID())
                .valor(new BigDecimal("150.00"))
                .dataPrevista(LocalDate.of(2026, 1, 26))
                .status(EnumStatusPagamento.ENVIADO_DLQ)
                .chaveIdempotencia("chave-1")
                .numeroParcela(2)
                .tentativas(3)
                .mensagemErro("Falha simulada na liquidacao")
                .build();
    }

    @Test
    void testAlertar_PagamentoEmDlq_EmiteAlerta() {
        PagamentoRecorrente pagamento = pagamentoEmDlq();
        when(pagamentoRepository.findNaoAlertados(EnumStatusPagamento.ENVIADO_DLQ))
                .thenReturn(List.of(pagamento));

        scanner.alertarPagamentosEmDlq();

        verify(alertaPort).alertar(eq(AlertaPort.TipoAlerta.PAGAMENTO_EM_DLQ), any());
    }

    @Test
    void testAlertar_MensagemContemContextoOperacional() {
        PagamentoRecorrente pagamento = pagamentoEmDlq();
        when(pagamentoRepository.findNaoAlertados(EnumStatusPagamento.ENVIADO_DLQ))
                .thenReturn(List.of(pagamento));

        scanner.alertarPagamentosEmDlq();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(alertaPort).alertar(any(), captor.capture());

        String mensagem = captor.getValue();
        assertTrue(mensagem.contains(pagamento.getId().toString()));
        assertTrue(mensagem.contains(pagamento.getAgendamentoId().toString()));
        assertTrue(mensagem.contains("parcela 2"));
        assertTrue(mensagem.contains("Falha simulada na liquidacao"));
    }

    @Test
    void testAlertar_MarcaAlertadoEm_ParaNaoRepetir() {
        PagamentoRecorrente pagamento = pagamentoEmDlq();
        when(pagamentoRepository.findNaoAlertados(EnumStatusPagamento.ENVIADO_DLQ))
                .thenReturn(List.of(pagamento));

        scanner.alertarPagamentosEmDlq();

        assertEquals(AGORA, pagamento.getAlertadoEm());
        verify(pagamentoRepository).save(pagamento);
    }

    @Test
    void testAlertar_JaAlertados_NaoRetornamNaConsulta() {
        when(pagamentoRepository.findNaoAlertados(EnumStatusPagamento.ENVIADO_DLQ))
                .thenReturn(List.of());

        scanner.alertarPagamentosEmDlq();

        verifyNoInteractions(alertaPort);
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void testAlertar_VariosPagamentos_UmAlertaPorPagamento() {
        List<PagamentoRecorrente> pagamentos = List.of(pagamentoEmDlq(), pagamentoEmDlq(), pagamentoEmDlq());
        when(pagamentoRepository.findNaoAlertados(EnumStatusPagamento.ENVIADO_DLQ))
                .thenReturn(pagamentos);

        scanner.alertarPagamentosEmDlq();

        verify(alertaPort, times(3)).alertar(eq(AlertaPort.TipoAlerta.PAGAMENTO_EM_DLQ), any());
        verify(pagamentoRepository, times(3)).save(any());
        pagamentos.forEach(p -> assertNotNull(p.getAlertadoEm()));
    }
}
