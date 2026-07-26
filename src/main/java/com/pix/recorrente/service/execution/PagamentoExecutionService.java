package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PagamentoExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(PagamentoExecutionService.class);
    private static final int MAX_TENTATIVAS = 3;

    private final PagamentoRecorrenteRepository pagamentoRepository;
    private final PixGatewaySimulator pixGateway;
    private final ProximaParcelaService proximaParcelaService;

    public PagamentoExecutionService(PagamentoRecorrenteRepository pagamentoRepository,
                                     PixGatewaySimulator pixGateway,
                                     ProximaParcelaService proximaParcelaService) {
        this.pagamentoRepository = pagamentoRepository;
        this.pixGateway = pixGateway;
        this.proximaParcelaService = proximaParcelaService;
    }

    public void executarPagamento(PagamentoRecorrente pagamento) {
        try {
            pagamento.setStatus(EnumStatusPagamento.PROCESSANDO);
            pagamentoRepository.save(pagamento);

            pixGateway.liquidar(pagamento);

            pagamento.marcarComoSucesso();
            pagamentoRepository.save(pagamento);

            proximaParcelaService.agendarProxima(pagamento);
        } catch (Exception e) {
            registrarFalha(pagamento, e);
        }
    }

    private void registrarFalha(PagamentoRecorrente pagamento, Exception e) {
        pagamento.marcarComoFalha(e.getMessage());

        if (pagamento.getTentativas() >= MAX_TENTATIVAS) {
            pagamento.setStatus(EnumStatusPagamento.ENVIADO_DLQ);
            logger.error("Pagamento {} esgotou {} tentativas e foi para a DLQ", pagamento.getId(), MAX_TENTATIVAS);
        } else {
            logger.warn("Pagamento {} falhou (tentativa {}). Proxima execucao: {}",
                    pagamento.getId(), pagamento.getTentativas(), pagamento.getProximaExecucao(), e);
        }

        pagamentoRepository.save(pagamento);
    }
}
