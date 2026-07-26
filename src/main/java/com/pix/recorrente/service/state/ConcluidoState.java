package com.pix.recorrente.service.state;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.model.Agendamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConcluidoState implements AgendamentoState {
    private static final Logger logger = LoggerFactory.getLogger(ConcluidoState.class);

    @Override
    public EnumStatusAgendamento status() {
        return EnumStatusAgendamento.CONCLUIDO;
    }

    @Override
    public void onEnter(Agendamento agendamento) {
        logger.info("Agendamento {} concluido: todas as {} parcelas foram executadas",
                agendamento.getId(), agendamento.getQuantidadeParcelas());
    }
}
