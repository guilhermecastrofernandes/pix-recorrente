package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PagamentoExecutionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(PagamentoExecutionScheduler.class);
    private final PagamentoRecorrenteRepository pagamentoRepository;
    private final PagamentoExecutionService pagamentoExecutionService;

    public PagamentoExecutionScheduler(PagamentoRecorrenteRepository pagamentoRepository,
                                       PagamentoExecutionService pagamentoExecutionService) {
        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoExecutionService = pagamentoExecutionService;
    }

    @Scheduled(fixedDelayString = "${scheduler.pagamento.delay:60000}", initialDelayString = "${scheduler.pagamento.initial-delay:5000}")
    public void executarPagamentosPendentes() {
        LocalDate hoje = LocalDate.now();
        List<PagamentoRecorrente> pagamentosPendentes = pagamentoRepository.findPendentes(EnumStatusPagamento.PENDENTE, hoje);

        logger.info("Iniciando execução de {} pagamentos pendentes", pagamentosPendentes.size());

        for (PagamentoRecorrente pagamento : pagamentosPendentes) {
            try {
                logger.debug("Executando pagamento ID: {} para agendamento: {}", pagamento.getId(), pagamento.getAgendamentoId());
                pagamentoExecutionService.executarPagamento(pagamento);
                logger.info("Pagamento ID: {} executado com sucesso", pagamento.getId());
            } catch (Exception e) {
                logger.error("Erro ao executar pagamento ID: {}", pagamento.getId(), e);
            }
        }

        logger.info("Execução de pagamentos pendentes finalizada");
    }
}
