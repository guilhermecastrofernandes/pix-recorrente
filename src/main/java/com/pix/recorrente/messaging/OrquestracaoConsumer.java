package com.pix.recorrente.messaging;

import com.pix.recorrente.config.RabbitMQConfig;
import com.pix.recorrente.service.orchestration.PagamentoRecorrenteOrchestrator;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Service
public class OrquestracaoConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrquestracaoConsumer.class);
    private final OrquestracaoPayloadConverter payloadConverter;
    private final PagamentoRecorrenteOrchestrator pagamentoOrchestrator;

    public OrquestracaoConsumer(OrquestracaoPayloadConverter payloadConverter,
                               PagamentoRecorrenteOrchestrator pagamentoOrchestrator) {
        this.payloadConverter = payloadConverter;
        this.pagamentoOrchestrator = pagamentoOrchestrator;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORQUESTRACAO, ackMode = "MANUAL")
    @Transactional
    public void consumirAgendamento(Map<String, Object> payload, Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            OrquestracaoPayload orquestracaoPayload = payloadConverter.convert(payload);
            pagamentoOrchestrator.criarOuIgnorar(orquestracaoPayload);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            logger.error("Erro ao processar agendamento. Tag: {}", tag, e);
            try {
                channel.basicNack(tag, false, false);
            } catch (IOException ioException) {
                logger.error("Erro ao fazer nack da mensagem", ioException);
            }
        }
    }
}
