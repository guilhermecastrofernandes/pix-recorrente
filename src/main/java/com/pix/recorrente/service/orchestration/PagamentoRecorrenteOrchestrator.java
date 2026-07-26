package com.pix.recorrente.service.orchestration;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.messaging.OrquestracaoPayload;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import com.pix.recorrente.service.execution.ChaveParcelaFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagamentoRecorrenteOrchestrator {
    private static final int PRIMEIRA_PARCELA = 1;

    private final PagamentoRecorrenteRepository pagamentoRepository;

    public PagamentoRecorrenteOrchestrator(PagamentoRecorrenteRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    public void criarOuIgnorar(OrquestracaoPayload payload) {
        String chaveParcela = ChaveParcelaFactory.chaveDaParcela(payload.chaveIdempotencia(), PRIMEIRA_PARCELA);

        if (pagamentoRepository.findByChaveIdempotencia(chaveParcela).isPresent()) {
            return;
        }

        PagamentoRecorrente pagamento = PagamentoRecorrente.builder()
                .agendamentoId(payload.agendamentoId())
                .valor(payload.valor())
                .dataPrevista(payload.dataInicio())
                .status(EnumStatusPagamento.PENDENTE)
                .chaveIdempotencia(chaveParcela)
                .numeroParcela(PRIMEIRA_PARCELA)
                .build();

        pagamentoRepository.save(pagamento);
    }
}
