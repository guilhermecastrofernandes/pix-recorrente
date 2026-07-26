package com.pix.recorrente.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pix.recorrente.domain.enums.EnumFrequencia;
import com.pix.recorrente.dto.AgendamentoRequest;
import com.pix.recorrente.messaging.OrquestracaoPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrquestracaoPublisher orquestracaoPublisher;

    private AgendamentoRequest request(String chavePix, BigDecimal valor) {
        return new AgendamentoRequest(
                "12345678900",
                chavePix,
                valor,
                EnumFrequencia.MENSAL,
                LocalDate.of(2026, 8, 1),
                12
        );
    }

    private org.springframework.test.web.servlet.ResultActions criar(AgendamentoRequest request, String chave) throws Exception {
        return mockMvc.perform(post("/v1/agendamentos")
                .header("X-Idempotency-Key", chave)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    @Test
    void postAgendamento_Aprovado_Retorna201EAtivo() throws Exception {
        criar(request("lojista@email.com", new BigDecimal("150.00")), "key-aprovado")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.analiseFraude.statusRisco").value("APROVADO"));
    }

    @Test
    void postAgendamento_ValorAcimaDoLimite_Retorna202EmAnalise() throws Exception {
        criar(request("lojista@email.com", new BigDecimal("6000.00")), "key-revisao")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"))
                .andExpect(jsonPath("$.analiseFraude.statusRisco").value("REVISAO_MANUAL"))
                .andExpect(jsonPath("$.analiseFraude.regrasVioladas[0]").value(
                        org.hamcrest.Matchers.containsString("RNF-01")));
    }

    @Test
    void postAgendamento_ChaveEmBlacklist_Retorna422() throws Exception {
        criar(request("fraudulento@banco.com.br", new BigDecimal("150.00")), "key-rejeitado")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("AGENDAMENTO_REJEITADO_FRAUDE"))
                .andExpect(jsonPath("$.analiseFraude.statusRisco").value("REJEITADO"));
    }

    @Test
    void postAgendamento_ChaveIdempotenciaRepetida_Retorna409() throws Exception {
        AgendamentoRequest req = request("lojista@email.com", new BigDecimal("150.00"));

        criar(req, "key-duplicada").andExpect(status().isCreated());
        criar(req, "key-duplicada").andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("AGENDAMENTO_DUPLICADO"));
    }

    @Test
    void postAgendamento_PayloadInvalido_Retorna400() throws Exception {
        AgendamentoRequest invalido = new AgendamentoRequest(
                "", "", new BigDecimal("-1"), EnumFrequencia.MENSAL, LocalDate.of(2026, 8, 1), 12);

        criar(invalido, "key-invalida")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACAO_ERRO"));
    }

    @Test
    void getAgendamento_Existente_Retorna200ComPagamentos() throws Exception {
        String body = criar(request("lojista@email.com", new BigDecimal("150.00")), "key-get")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(body).get("id").asText();

        mockMvc.perform(get("/v1/agendamentos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.pagamentos").isArray());
    }

    @Test
    void getAgendamento_Inexistente_Retorna404() throws Exception {
        mockMvc.perform(get("/v1/agendamentos/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("AGENDAMENTO_NAO_ENCONTRADO"));
    }
}
