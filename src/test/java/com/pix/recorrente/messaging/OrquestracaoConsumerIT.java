package com.pix.recorrente.messaging;

import com.pix.recorrente.config.RabbitMQConfig;
import com.pix.recorrente.repository.PagamentoRecorrenteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class OrquestracaoConsumerIT {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.12-management");

    @Autowired
    private PagamentoRecorrenteRepository pagamentoRepository;

    @Test
    void testConsumerContextLoads() {
        // Teste básico: verifica se o app inicializa com consumer rodando
        assertNotNull(pagamentoRepository);
    }
}
