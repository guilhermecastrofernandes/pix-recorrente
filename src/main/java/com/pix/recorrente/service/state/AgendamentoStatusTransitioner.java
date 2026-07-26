package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import com.pix.recorrente.domain.model.Agendamento;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AgendamentoStatusTransitioner {
    private final Map<EnumStatusAgendamento, AgendamentoState> stateMap;

    public AgendamentoStatusTransitioner(List<AgendamentoState> states) {
        this.stateMap = states.stream()
                .collect(Collectors.toMap(AgendamentoState::status, Function.identity()));
    }

    public void transicionar(Agendamento agendamento, EnumStatusRisco statusRisco) {
        transicionarPara(agendamento, mapStatusRiscoToAgendamento(statusRisco));
    }

    public void transicionarPara(Agendamento agendamento, EnumStatusAgendamento novoStatus) {
        AgendamentoState state = stateMap.get(novoStatus);
        if (state == null) {
            throw new IllegalStateException("Nenhum AgendamentoState registrado para o status " + novoStatus);
        }

        agendamento.setStatus(novoStatus);
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
