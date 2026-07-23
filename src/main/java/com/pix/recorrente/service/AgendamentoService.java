package com.pix.recorrente.service;

import com.pix.recorrente.domain.enums.EnumStatusRisco;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.domain.model.AnaliseFraude;
import com.pix.recorrente.dto.AgendamentoRequest;
import com.pix.recorrente.exception.AgendamentoDuplicadoException;
import com.pix.recorrente.exception.AgendamentoNaoEncontradoException;
import com.pix.recorrente.exception.AgendamentoRejeitadoFraudeException;
import com.pix.recorrente.repository.AgendamentoRepository;
import com.pix.recorrente.service.mapper.AgendamentoMapper;
import com.pix.recorrente.service.serialization.AnaliseFraudeJsonSerializer;
import com.pix.recorrente.service.state.AgendamentoStatusTransitioner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AgendamentoService {
    private final AgendamentoRepository agendamentoRepository;
    private final AntifraudeService antifraudeService;
    private final AgendamentoStatusTransitioner statusTransitioner;
    private final AgendamentoMapper agendamentoMapper;
    private final AnaliseFraudeJsonSerializer analiseFraudeJsonSerializer;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                              AntifraudeService antifraudeService,
                              AgendamentoStatusTransitioner statusTransitioner,
                              AgendamentoMapper agendamentoMapper,
                              AnaliseFraudeJsonSerializer analiseFraudeJsonSerializer) {
        this.agendamentoRepository = agendamentoRepository;
        this.antifraudeService = antifraudeService;
        this.statusTransitioner = statusTransitioner;
        this.agendamentoMapper = agendamentoMapper;
        this.analiseFraudeJsonSerializer = analiseFraudeJsonSerializer;
    }

    @Transactional(noRollbackFor = {AgendamentoRejeitadoFraudeException.class, AgendamentoDuplicadoException.class})
    public Agendamento criarAgendamento(AgendamentoRequest request, String chaveIdempotencia) {
        var existente = agendamentoRepository.findByChaveIdempotencia(chaveIdempotencia);
        if (existente.isPresent()) {
            throw new AgendamentoDuplicadoException("Um agendamento com esta chave de idempotência já foi processado.", existente.get());
        }

        AnaliseFraude analiseFraude = antifraudeService.analisar(request.chavePixRecebedor(), request.valor());
        String analiseFraudeJson = analiseFraudeJsonSerializer.serialize(analiseFraude);

        Agendamento agendamento = agendamentoMapper.toEntity(request, chaveIdempotencia, analiseFraudeJson);

        if (analiseFraude.statusRisco() == EnumStatusRisco.REJEITADO) {
            agendamento.setStatus(agendamento.getStatus());
            agendamentoRepository.save(agendamento);
            throw new AgendamentoRejeitadoFraudeException("Agendamento rejeitado pelas regras de fraude", analiseFraude);
        }

        agendamentoRepository.saveAndFlush(agendamento);
        statusTransitioner.transicionar(agendamento, analiseFraude.statusRisco());
        return agendamento;
    }

    @Transactional(readOnly = true)
    public Agendamento obterAgendamentoPorId(UUID id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new AgendamentoNaoEncontradoException("Nenhum agendamento localizado para o ID informado."));
    }

    public AnaliseFraude parseAnaliseFraude(String json) {
        return analiseFraudeJsonSerializer.deserialize(json);
    }
}
