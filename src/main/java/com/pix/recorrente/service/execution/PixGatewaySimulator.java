package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.model.PagamentoRecorrente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Simula a chamada ao SPI/DICT. Em producao daria lugar a um HTTP client com
 * timeout e circuit breaker; aqui a taxa de falha e configuravel para que o
 * caminho de retry/DLQ seja exercitavel em testes e na demo.
 */
@Component
public class PixGatewaySimulator {
    private final double taxaFalha;
    private final long latenciaMs;

    public PixGatewaySimulator(@Value("${pix.simulacao.taxa-falha:0.0}") double taxaFalha,
                               @Value("${pix.simulacao.latencia-ms:100}") long latenciaMs) {
        this.taxaFalha = taxaFalha;
        this.latenciaMs = latenciaMs;
    }

    public void liquidar(PagamentoRecorrente pagamento) {
        try {
            Thread.sleep(latenciaMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PixGatewayException("Processamento interrompido", e);
        }

        if (taxaFalha > 0 && ThreadLocalRandom.current().nextDouble() < taxaFalha) {
            throw new PixGatewayException("Falha simulada na liquidacao do pagamento " + pagamento.getId());
        }
    }
}
