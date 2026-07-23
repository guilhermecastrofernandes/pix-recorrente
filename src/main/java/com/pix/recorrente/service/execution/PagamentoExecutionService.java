package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class PagamentoExecutionService {
    private final PagamentoRecorrenteRepository pagamentoRepository;

    public PagamentoExecutionService(PagamentoRecorrenteRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    public void executarPagamento(PagamentoRecorrente pagamento) {
        try {
            pagamento.setStatus(EnumStatusPagamento.PROCESSANDO);
            pagamentoRepository.save(pagamento);

            simulaProcessamentoPix(pagamento);

            pagamento.setStatus(EnumStatusPagamento.SUCESSO);
            pagamento.setDataExecucao(LocalDateTime.now());
            pagamentoRepository.save(pagamento);
        } catch (Exception e) {
            pagamento.setStatus(EnumStatusPagamento.FALHA_PROCESSAMENTO);
            pagamento.setMensagemErro(e.getMessage());
            pagamentoRepository.save(pagamento);
            throw e;
        }
    }

    private void simulaProcessamentoPix(PagamentoRecorrente pagamento) {
        // Simula chamada a SPI/DICT ou sistema de pagamentos
        // Em produção: httpClient.post("https://spi.bacen/pix/send", payload)
        // Para PoC: apenas delay de 100ms
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processamento interrompido", e);
        }
    }
}
