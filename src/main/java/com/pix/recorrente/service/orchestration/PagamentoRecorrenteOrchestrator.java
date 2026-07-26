package com.pix.recorrente.service.orchestration;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.messaging.OrquestracaoPayload;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class PagamentoRecorrenteOrchestrator {
    private final PagamentoRecorrenteRepository pagamentoRepository;

    public PagamentoRecorrenteOrchestrator(PagamentoRecorrenteRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    public void criarOuIgnorar(OrquestracaoPayload payload) {
        if (pagamentoRepository.findByChaveIdempotencia(payload.chaveIdempotencia()).isPresent()) {
            return;
        }

        PagamentoRecorrente pagamento = PagamentoRecorrente.builder()
                .agendamentoId(payload.agendamentoId())
                .valor(payload.valor())
                .dataPrevista(payload.dataInicio())
                .status(EnumStatusPagamento.PENDENTE)
                .chaveIdempotencia(payload.chaveIdempotencia())
                .build();

        pagamentoRepository.save(pagamento);
    }
}
