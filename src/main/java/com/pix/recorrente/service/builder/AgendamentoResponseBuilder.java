package com.pix.recorrente.service.builder;

import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.dto.AgendamentoResponse;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import com.pix.recorrente.service.serialization.AnaliseFraudeJsonSerializer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgendamentoResponseBuilder {
    private final PagamentoRecorrenteRepository pagamentoRepository;
    private final AnaliseFraudeJsonSerializer analiseFraudeJsonSerializer;

    public AgendamentoResponseBuilder(PagamentoRecorrenteRepository pagamentoRepository,
                                     AnaliseFraudeJsonSerializer analiseFraudeJsonSerializer) {
        this.pagamentoRepository = pagamentoRepository;
        this.analiseFraudeJsonSerializer = analiseFraudeJsonSerializer;
    }

    public AgendamentoResponse build(Agendamento agendamento) {
        var analiseFraude = analiseFraudeJsonSerializer.deserialize(agendamento.getAnaliseFraudeJson());
        var analiseFraudeResponse = new AgendamentoResponse.AnaliseFraudeResponse(
            analiseFraude.statusRisco(),
            analiseFraude.score(),
            analiseFraude.regrasVioladas(),
            analiseFraude.dataAnalise()
        );

        List<PagamentoRecorrente> pagamentos = pagamentoRepository.findByAgendamentoId(agendamento.getId());
        var pagamentosResponse = pagamentos.stream()
            .map(p -> new AgendamentoResponse.PagamentoResponse(
                p.getId(),
                p.getDataPrevista(),
                p.getValor(),
                p.getStatus(),
                p.getDataExecucao()
            ))
            .toList();

        return new AgendamentoResponse(
            agendamento.getId(),
            agendamento.getClienteId(),
            agendamento.getChavePixRecebedor(),
            agendamento.getValor(),
            agendamento.getFrequencia(),
            agendamento.getDataInicio(),
            agendamento.getQuantidadeParcelas(),
            agendamento.getStatus(),
            analiseFraudeResponse,
            agendamento.getDataCriacao(),
            pagamentosResponse
        );
    }
}
