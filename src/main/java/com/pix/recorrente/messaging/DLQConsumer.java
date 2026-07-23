package com.pix.recorrente.messaging;

import com.pix.recorrente.config.RabbitMQConfig;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class DLQConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DLQConsumer.class);
    private final RabbitTemplate rabbitTemplate;

    public DLQConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DLQ, ackMode = "MANUAL")
    public void consumirDLQ(Map<String, Object> payload, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            logger.warn("Mensagem na DLQ: {}", payload);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ORQUESTRACAO,
                RabbitMQConfig.ROUTING_KEY, payload);
            logger.info("Mensagem recolocada na fila principal para retry");
            channel.basicAck(tag, false);
        } catch (Exception e) {
            logger.error("Erro ao reprocessar mensagem da DLQ", e);
            try {
                channel.basicNack(tag, false, false);
            } catch (IOException ioException) {
                logger.error("Erro ao fazer nack da mensagem DLQ", ioException);
            }
        }
    }
}
