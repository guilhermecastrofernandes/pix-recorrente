package com.pix.recorrente.messaging;

import com.pix.recorrente.config.RabbitMQConfig;
import com.pix.recorrente.service.alerta.AlertaPort;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class DLQConsumer {
    private final AlertaPort alertaPort;

    public DLQConsumer(AlertaPort alertaPort) {
        this.alertaPort = alertaPort;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DLQ)
    public void consumirMensagemDescartada(Map<String, Object> payload) {
        alertaPort.alertar(AlertaPort.TipoAlerta.MENSAGEM_DESCARTADA, montarMensagem(payload));
    }

    private String montarMensagem(Map<String, Object> payload) {
        Object agendamentoId = payload != null ? payload.get("id") : null;
        Object chaveIdempotencia = payload != null ? payload.get("chaveIdempotencia") : null;

        return String.format(
                "Mensagem de orquestracao descartada. Agendamento: %s, chaveIdempotencia: %s. "
                        + "Nenhuma parcela foi criada para este agendamento. Payload: %s",
                agendamentoId, chaveIdempotencia, payload);
    }
}
