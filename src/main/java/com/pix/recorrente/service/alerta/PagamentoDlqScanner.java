package com.pix.recorrente.service.alerta;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagamentoDlqScanner {
    private final PagamentoRecorrenteRepository pagamentoRepository;
    private final AlertaPort alertaPort;
    private final Clock clock;

    public PagamentoDlqScanner(PagamentoRecorrenteRepository pagamentoRepository,
                               AlertaPort alertaPort,
                               Clock clock) {
        this.pagamentoRepository = pagamentoRepository;
        this.alertaPort = alertaPort;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${scheduler.dlq-scanner.delay:60000}",
               initialDelayString = "${scheduler.dlq-scanner.initial-delay:10000}")
    @Transactional
    public void alertarPagamentosEmDlq() {
        List<PagamentoRecorrente> naoAlertados =
                pagamentoRepository.findNaoAlertados(EnumStatusPagamento.ENVIADO_DLQ);

        if (naoAlertados.isEmpty()) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now(clock);

        for (PagamentoRecorrente pagamento : naoAlertados) {
            alertaPort.alertar(AlertaPort.TipoAlerta.PAGAMENTO_EM_DLQ, montarMensagem(pagamento));
            pagamento.marcarComoAlertado(agora);
            pagamentoRepository.save(pagamento);
        }
    }

    private String montarMensagem(PagamentoRecorrente pagamento) {
        return String.format(
                "Pagamento %s (parcela %d do agendamento %s, valor %s) esgotou as tentativas de liquidacao. "
                        + "A recorrencia esta interrompida ate reprocessamento manual. Ultimo erro: %s",
                pagamento.getId(),
                pagamento.getNumeroParcela(),
                pagamento.getAgendamentoId(),
                pagamento.getValor(),
                pagamento.getMensagemErro());
    }
}
