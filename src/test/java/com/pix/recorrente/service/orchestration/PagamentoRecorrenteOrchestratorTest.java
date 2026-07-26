package com.pix.recorrente.service.orchestration;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.messaging.OrquestracaoPayload;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoRecorrenteOrchestratorTest {

    private static final String CHAVE_AGENDAMENTO = "idem-key-123";
    private static final String CHAVE_PARCELA_1 = "idem-key-123-1";

    @Mock
    private PagamentoRecorrenteRepository pagamentoRepository;

    @InjectMocks
    private PagamentoRecorrenteOrchestrator orchestrator;

    private UUID agendamentoId;
    private OrquestracaoPayload validPayload;

    @BeforeEach
    void setup() {
        agendamentoId = UUID.randomUUID();
        validPayload = new OrquestracaoPayload(
                agendamentoId,
                "cliente-123",
                new BigDecimal("150.00"),
                LocalDate.of(2026, 8, 1),
                "usuario@email.com",
                "MENSAL",
                12,
                CHAVE_AGENDAMENTO
        );
    }

    private PagamentoRecorrente capturarSalvo() {
        ArgumentCaptor<PagamentoRecorrente> captor = ArgumentCaptor.forClass(PagamentoRecorrente.class);
        verify(pagamentoRepository).save(captor.capture());
        return captor.getValue();
    }

    @Test
    void testCriarOuIgnorar_NovaParcela_Criada() {
        when(pagamentoRepository.findByChaveIdempotencia(CHAVE_PARCELA_1)).thenReturn(Optional.empty());

        orchestrator.criarOuIgnorar(validPayload);

        PagamentoRecorrente salvo = capturarSalvo();
        assertEquals(EnumStatusPagamento.PENDENTE, salvo.getStatus());
        assertEquals(agendamentoId, salvo.getAgendamentoId());
        assertEquals(new BigDecimal("150.00"), salvo.getValor());
        assertEquals(LocalDate.of(2026, 8, 1), salvo.getDataPrevista());
    }

    @Test
    void testCriarOuIgnorar_PrimeiraParcela_NumeroUm() {
        when(pagamentoRepository.findByChaveIdempotencia(CHAVE_PARCELA_1)).thenReturn(Optional.empty());

        orchestrator.criarOuIgnorar(validPayload);

        assertEquals(1, capturarSalvo().getNumeroParcela());
    }

    @Test
    void testCriarOuIgnorar_ChaveDaParcelaRecebeSufixo() {
        when(pagamentoRepository.findByChaveIdempotencia(CHAVE_PARCELA_1)).thenReturn(Optional.empty());

        orchestrator.criarOuIgnorar(validPayload);

        assertEquals(CHAVE_PARCELA_1, capturarSalvo().getChaveIdempotencia());
    }

    @Test
    void testCriarOuIgnorar_Redelivery_Ignorada() {
        PagamentoRecorrente existente = PagamentoRecorrente.builder()
                .id(UUID.randomUUID())
                .chaveIdempotencia(CHAVE_PARCELA_1)
                .numeroParcela(1)
                .status(EnumStatusPagamento.SUCESSO)
                .build();

        when(pagamentoRepository.findByChaveIdempotencia(CHAVE_PARCELA_1)).thenReturn(Optional.of(existente));

        orchestrator.criarOuIgnorar(validPayload);

        verify(pagamentoRepository, never()).save(any());
    }
}
