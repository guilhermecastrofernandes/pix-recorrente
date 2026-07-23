package com.pix.recorrente.messaging;

import com.pix.recorrente.config.RabbitMQConfig;
import com.pix.recorrente.domain.model.Agendamento;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrquestracaoPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final OrquestracaoPayloadBuilder payloadBuilder;

    public OrquestracaoPublisher(RabbitTemplate rabbitTemplate, OrquestracaoPayloadBuilder payloadBuilder) {
        this.rabbitTemplate = rabbitTemplate;
        this.payloadBuilder = payloadBuilder;
    }

    public void publicarAgendamento(Agendamento agendamento) {
        Map<String, Object> payload = payloadBuilder.build(agendamento);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ORQUESTRACAO, RabbitMQConfig.ROUTING_KEY, payload);
    }
}
