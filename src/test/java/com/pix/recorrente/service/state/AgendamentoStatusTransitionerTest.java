package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import com.pix.recorrente.domain.model.Agendamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendamentoStatusTransitionerTest {

    @Mock
    private AtivoState ativoState;

    @Mock
    private EmAnaliseState emAnaliseState;

    @Mock
    private RejeitadoFraudeState rejeitadoFraudeState;

    @Mock
    private CanceladoState canceladoState;

    @Mock
    private ConcluidoState concluidoState;

    private AgendamentoStatusTransitioner statusTransitioner;
    private Agendamento agendamento;

    @BeforeEach
    void setup() {
        when(ativoState.status()).thenReturn(EnumStatusAgendamento.ATIVO);
        when(emAnaliseState.status()).thenReturn(EnumStatusAgendamento.EM_ANALISE);
        when(rejeitadoFraudeState.status()).thenReturn(EnumStatusAgendamento.REJEITADO_FRAUDE);
        when(canceladoState.status()).thenReturn(EnumStatusAgendamento.CANCELADO);
        when(concluidoState.status()).thenReturn(EnumStatusAgendamento.CONCLUIDO);

        statusTransitioner = new AgendamentoStatusTransitioner(
                List.of(ativoState, emAnaliseState, rejeitadoFraudeState, canceladoState, concluidoState)
        );

        agendamento = Agendamento.builder()
                .id(UUID.randomUUID())
                .clienteId("cliente-123")
                .chavePixRecebedor("usuario@email.com")
                .valor(new BigDecimal("1000.00"))
                .dataCriacao(LocalDateTime.now())
                .chaveIdempotencia("idem-key")
                .status(EnumStatusAgendamento.EM_ANALISE)
                .build();
    }

    @Test
    void testTransicionar_Approved_ToAtivo() {
        statusTransitioner.transicionar(agendamento, EnumStatusRisco.APROVADO);

        assertEquals(EnumStatusAgendamento.ATIVO, agendamento.getStatus());
        verify(ativoState).onEnter(agendamento);
    }

    @Test
    void testTransicionar_ManualReview_ToEmAnalise() {
        statusTransitioner.transicionar(agendamento, EnumStatusRisco.REVISAO_MANUAL);

        assertEquals(EnumStatusAgendamento.EM_ANALISE, agendamento.getStatus());
        verify(emAnaliseState).onEnter(agendamento);
    }

    @Test
    void testTransicionar_Rejected_ToRejeitadoFraude() {
        statusTransitioner.transicionar(agendamento, EnumStatusRisco.REJEITADO);

        assertEquals(EnumStatusAgendamento.REJEITADO_FRAUDE, agendamento.getStatus());
        verify(rejeitadoFraudeState).onEnter(agendamento);
    }

    @Test
    void testTransicionarPara_Concluido_InvokesConcluidoState() {
        statusTransitioner.transicionarPara(agendamento, EnumStatusAgendamento.CONCLUIDO);

        assertEquals(EnumStatusAgendamento.CONCLUIDO, agendamento.getStatus());
        verify(concluidoState).onEnter(agendamento);
    }

    @Test
    void testTransicionar_TodosOsStatusDeRiscoTemStateRegistrado() {
        for (EnumStatusRisco risco : EnumStatusRisco.values()) {
            statusTransitioner.transicionar(agendamento, risco);
        }
    }

    @Test
    void testTransicionarPara_StatusSemStateRegistrado_Falha() {
        AgendamentoStatusTransitioner incompleto =
                new AgendamentoStatusTransitioner(List.of(ativoState));

        assertThrows(IllegalStateException.class,
                () -> incompleto.transicionarPara(agendamento, EnumStatusAgendamento.CONCLUIDO));
    }
}
