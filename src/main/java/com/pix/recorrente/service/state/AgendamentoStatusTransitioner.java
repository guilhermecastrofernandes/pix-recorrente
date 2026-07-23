package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import com.pix.recorrente.domain.model.Agendamento;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AgendamentoStatusTransitioner {
    private final Map<EnumStatusAgendamento, AgendamentoState> stateMap;

    public AgendamentoStatusTransitioner(AtivoState ativoState,
                                         EmAnaliseState emAnaliseState,
                                         RejeitadoFraudeState rejeitadoFraudeState,
                                         CanceladoState canceladoState) {
        this.stateMap = Map.of(
            EnumStatusAgendamento.ATIVO, ativoState,
            EnumStatusAgendamento.EM_ANALISE, emAnaliseState,
            EnumStatusAgendamento.REJEITADO_FRAUDE, rejeitadoFraudeState,
            EnumStatusAgendamento.CANCELADO, canceladoState
        );
    }

    public void transicionar(Agendamento agendamento, EnumStatusRisco statusRisco) {
        EnumStatusAgendamento novoStatus = mapStatusRiscoToAgendamento(statusRisco);
        agendamento.setStatus(novoStatus);

        AgendamentoState state = stateMap.get(novoStatus);
        state.onEnter(agendamento);
    }

    private EnumStatusAgendamento mapStatusRiscoToAgendamento(EnumStatusRisco statusRisco) {
        return switch (statusRisco) {
            case APROVADO -> EnumStatusAgendamento.ATIVO;
            case REVISAO_MANUAL -> EnumStatusAgendamento.EM_ANALISE;
            case REJEITADO -> EnumStatusAgendamento.REJEITADO_FRAUDE;
        };
    }
}
