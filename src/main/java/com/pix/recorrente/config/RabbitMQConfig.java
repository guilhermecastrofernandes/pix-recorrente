package com.pix.recorrente.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_ORQUESTRACAO = "pix.agendamento.exchange";
    public static final String QUEUE_ORQUESTRACAO = "pix.agendamento.orquestracao";
    public static final String QUEUE_DLQ = "pix.agendamento.orquestracao.dlq";
    public static final String ROUTING_KEY = "agendamento.criado";

    @Bean
    public DirectExchange orquestracaoExchange() {
        return new DirectExchange(EXCHANGE_ORQUESTRACAO, true, false);
    }

    @Bean
    public Queue orquestracaoQueue() {
        return QueueBuilder.durable(QUEUE_ORQUESTRACAO)
                .deadLetterExchange(EXCHANGE_ORQUESTRACAO)
                .deadLetterRoutingKey("dlq.agendamento")
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding orquestracaoBinding(Queue orquestracaoQueue, DirectExchange orquestracaoExchange) {
        return BindingBuilder.bind(orquestracaoQueue).to(orquestracaoExchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding(Queue dlqQueue, DirectExchange orquestracaoExchange) {
        return BindingBuilder.bind(dlqQueue).to(orquestracaoExchange).with("dlq.agendamento");
    }
}
