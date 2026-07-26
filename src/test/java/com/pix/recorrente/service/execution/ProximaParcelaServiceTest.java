package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.enums.EnumFrequencia;
import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.AgendamentoRepository;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import com.pix.recorrente.service.state.AgendamentoStatusTransitioner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProximaParcelaServiceTest {

    private static final String CHAVE_AGENDAMENTO = "idem-key-abc";

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private PagamentoRecorrenteRepository pagamentoRepository;

    @Mock
    private AgendamentoStatusTransitioner statusTransitioner;

    @InjectMocks
    private ProximaParcelaService proximaParcelaService;

    private UUID agendamentoId;

    @BeforeEach
    void setup() {
        agendamentoId = UUID.randomUUID();
    }

    private Agendamento agendamento(EnumFrequencia frequencia, Integer quantidadeParcelas, EnumStatusAgendamento status) {
        return Agendamento.builder()
                .id(agendamentoId)
                .clienteId("cliente-1")
                .chavePixRecebedor("lojista@email.com")
                .valor(new BigDecimal("150.00"))
                .frequencia(frequencia)
                .dataInicio(LocalDate.of(2026, 3, 1))
                .quantidadeParcelas(quantidadeParcelas)
                .status(status)
                .chaveIdempotencia(CHAVE_AGENDAMENTO)
                .dataCriacao(LocalDateTime.now())
                .build();
    }

    private PagamentoRecorrente parcela(int numero, LocalDate dataPrevista) {
        return PagamentoRecorrente.builder()
                .id(UUID.randomUUID())
                .agendamentoId(agendamentoId)
                .valor(new BigDecimal("150.00"))
                .dataPrevista(dataPrevista)
                .status(EnumStatusPagamento.SUCESSO)
                .chaveIdempotencia(ChaveParcelaFactory.chaveDaParcela(CHAVE_AGENDAMENTO, numero))
                .numeroParcela(numero)
                .build();
    }

    private PagamentoRecorrente capturarSalvo() {
        ArgumentCaptor<PagamentoRecorrente> captor = ArgumentCaptor.forClass(PagamentoRecorrente.class);
        verify(pagamentoRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void testAgendarProxima_Mensal_SomaUmMes() {
        when(agendamentoRepository.findById(agendamentoId))
                .thenReturn(Optional.of(agendamento(EnumFrequencia.MENSAL, 12, EnumStatusAgendamento.ATIVO)));
        when(pagamentoRepository.findByChaveIdempotencia(CHAVE_AGENDAMENTO + "-2")).thenReturn(Optional.empty());

        proximaParcelaService.agendarProxima(parcela(1, LocalDate.of(2026, 3, 1)));

        PagamentoRecorrente proxima = capturarSalvo();
        assertEquals(LocalDate.of(2026, 4, 1), proxima.getDataPrevista());
        assertEquals(2, proxima.getNumeroParcela());
        assertEquals(CHAVE_AGENDAMENTO + "-2", proxima.getChaveIdempotencia());
        assertEquals(EnumStatusPagamento.PENDENTE, proxima.getStatus());
    }

    @Test
    void testAgendarProxima_FimDeMes_TruncaParaUltimoDiaValido() {
        when(agendamentoRepository.findById(agendamentoId))
                .thenReturn(Optional.of(agendamento(EnumFrequencia.MENSAL, 12, EnumStatusAgendamento.ATIVO)));
        when(pagamentoRepository.findByChaveIdempotencia(CHAVE_AGENDAMENTO + "-2")).thenReturn(Optional.empty());

        proximaParcelaService.agendarProxima(parcela(1, LocalDate.of(2026, 1, 31)));

        assertEquals(LocalDate.of(2026, 2, 28), capturarSalvo().getDataPrevista());
    }

    @Test
    void testAgendarProxima_UltimaParcela_ConcluiAgendamentoENaoGeraNova() {
        Agendamento ag = agendamento(EnumFrequencia.MENSAL, 6, EnumStatusAgendamento.ATIVO);
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(ag));

        proximaParcelaService.agendarProxima(parcela(6, LocalDate.of(2026, 8, 1)));

        verify(statusTransitioner).transicionarPara(ag, EnumStatusAgendamento.CONCLUIDO);
        verify(agendamentoRepository).save(ag);
        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void testAgendarProxima_QuantidadeParcelasNula_RecorrenciaIlimitada() {
        when(agendamentoRepository.findById(agendamentoId))
                .thenReturn(Optional.of(agendamento(EnumFrequencia.MENSAL, null, EnumStatusAgendamento.ATIVO)));
        when(pagamentoRepository.findByChaveIdempotencia(CHAVE_AGENDAMENTO + "-100")).thenReturn(Optional.empty());

        proximaParcelaService.agendarProxima(parcela(99, LocalDate.of(2026, 3, 1)));

        assertEquals(100, capturarSalvo().getNumeroParcela());
        verify(statusTransitioner, never()).transicionarPara(any(), any());
    }

    @Test
    void testAgendarProxima_AgendamentoNaoAtivo_NaoGeraParcela() {
        when(agendamentoRepository.findById(agendamentoId))
                .thenReturn(Optional.of(agendamento(EnumFrequencia.MENSAL, 12, EnumStatusAgendamento.CANCELADO)));

        proximaParcelaService.agendarProxima(parcela(1, LocalDate.of(2026, 3, 1)));

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void testAgendarProxima_ProximaParcelaJaExiste_NaoDuplica() {
        when(agendamentoRepository.findById(agendamentoId))
                .thenReturn(Optional.of(agendamento(EnumFrequencia.MENSAL, 12, EnumStatusAgendamento.ATIVO)));
        when(pagamentoRepository.findByChaveIdempotencia(CHAVE_AGENDAMENTO + "-2"))
                .thenReturn(Optional.of(parcela(2, LocalDate.of(2026, 4, 1))));

        proximaParcelaService.agendarProxima(parcela(1, LocalDate.of(2026, 3, 1)));

        verify(pagamentoRepository, never()).save(any());
    }

    @Test
    void testAgendarProxima_AgendamentoInexistente_NaoQuebra() {
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.empty());

        proximaParcelaService.agendarProxima(parcela(1, LocalDate.of(2026, 3, 1)));

        verify(pagamentoRepository, never()).save(any());
    }
}
