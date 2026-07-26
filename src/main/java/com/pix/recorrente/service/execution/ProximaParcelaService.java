package com.pix.recorrente.service.execution;

import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.domain.model.PagamentoRecorrente;
import com.pix.recorrente.repository.AgendamentoRepository;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import com.pix.recorrente.service.state.AgendamentoStatusTransitioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class ProximaParcelaService {
    private static final Logger logger = LoggerFactory.getLogger(ProximaParcelaService.class);

    private final AgendamentoRepository agendamentoRepository;
    private final PagamentoRecorrenteRepository pagamentoRepository;
    private final AgendamentoStatusTransitioner statusTransitioner;

    public ProximaParcelaService(AgendamentoRepository agendamentoRepository,
                                 PagamentoRecorrenteRepository pagamentoRepository,
                                 AgendamentoStatusTransitioner statusTransitioner) {
        this.agendamentoRepository = agendamentoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.statusTransitioner = statusTransitioner;
    }

    public void agendarProxima(PagamentoRecorrente parcelaLiquidada) {
        Agendamento agendamento = agendamentoRepository.findById(parcelaLiquidada.getAgendamentoId()).orElse(null);
        if (agendamento == null) {
            logger.warn("Agendamento {} nao encontrado; recorrencia interrompida", parcelaLiquidada.getAgendamentoId());
            return;
        }

        if (agendamento.getStatus() != EnumStatusAgendamento.ATIVO) {
            logger.debug("Agendamento {} esta {}; nao gera nova parcela", agendamento.getId(), agendamento.getStatus());
            return;
        }

        int proximoNumero = parcelaLiquidada.getNumeroParcela() + 1;
        Integer total = agendamento.getQuantidadeParcelas();

        if (total != null && parcelaLiquidada.getNumeroParcela() >= total) {
            statusTransitioner.transicionarPara(agendamento, EnumStatusAgendamento.CONCLUIDO);
            agendamentoRepository.save(agendamento);
            return;
        }

        String chaveProxima = ChaveParcelaFactory.chaveDaParcela(agendamento.getChaveIdempotencia(), proximoNumero);
        if (pagamentoRepository.findByChaveIdempotencia(chaveProxima).isPresent()) {
            return;
        }

        LocalDate proximaData = agendamento.getFrequencia().proximaData(parcelaLiquidada.getDataPrevista());

        PagamentoRecorrente proxima = PagamentoRecorrente.builder()
                .agendamentoId(agendamento.getId())
                .valor(agendamento.getValor())
                .dataPrevista(proximaData)
                .status(EnumStatusPagamento.PENDENTE)
                .chaveIdempotencia(chaveProxima)
                .numeroParcela(proximoNumero)
                .build();

        pagamentoRepository.save(proxima);
        logger.info("Parcela {} do agendamento {} agendada para {}", proximoNumero, agendamento.getId(), proximaData);
    }
}
