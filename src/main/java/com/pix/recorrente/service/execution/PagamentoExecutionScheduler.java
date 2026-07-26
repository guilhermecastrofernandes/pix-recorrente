package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        List<PagamentoRecorrente> pendentes = pagamentoRepository.findPendentes(EnumStatusPagamento.PENDENTE, LocalDate.now());
        List<PagamentoRecorrente> retries = pagamentoRepository.findComRetry(EnumStatusPagamento.FALHA_PROCESSAMENTO, LocalDateTime.now());

        if (pendentes.isEmpty() && retries.isEmpty()) {
            return;
        }

        logger.info("Iniciando execução de {} pendentes + {} retries", pendentes.size(), retries.size());

        List<PagamentoRecorrente> aExecutar = new ArrayList<>(pendentes);
        aExecutar.addAll(retries);

        for (PagamentoRecorrente pagamento : aExecutar) {
            try {
                logger.debug("Executando pagamento ID: {} (tentativa: {}) para agendamento: {}",
                    pagamento.getId(), pagamento.getTentativas(), pagamento.getAgendamentoId());
                pagamentoExecutionService.executarPagamento(pagamento);
                logger.info("Pagamento ID: {} executado com sucesso", pagamento.getId());
            } catch (Exception e) {
                logger.error("Erro ao executar pagamento ID: {}", pagamento.getId(), e);
            }
        }

        logger.info("Execução de pagamentos finalizada");
    }
}
