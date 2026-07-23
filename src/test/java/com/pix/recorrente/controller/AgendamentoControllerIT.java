package com.pix.recorrente.controller;

import com.pix.recorrente.dto.AgendamentoRequest;
import com.pix.recorrente.domain.enums.EnumFrequencia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class AgendamentoControllerIT {

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.12-management");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateAgendamento_Success() throws Exception {
        AgendamentoRequest request = new AgendamentoRequest(
                "cliente-123",
                "12345678901234567890",
                new BigDecimal("1000.00"),
                EnumFrequencia.MENSAL,
                LocalDate.now().plusDays(1),
                12
        );

        mockMvc.perform(post("/v1/agendamentos")
                .header("X-Idempotency-Key", "idempotency-key-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testCreateAgendamento_InvalidRequest() throws Exception {
        AgendamentoRequest request = new AgendamentoRequest(
                "",
                "12345678901234567890",
                new BigDecimal("1000.00"),
                EnumFrequencia.MENSAL,
                LocalDate.now().plusDays(1),
                12
        );

        mockMvc.perform(post("/v1/agendamentos")
                .header("X-Idempotency-Key", "idempotency-key-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
