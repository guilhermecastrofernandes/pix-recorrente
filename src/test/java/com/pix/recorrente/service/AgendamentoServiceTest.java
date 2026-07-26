package com.pix.recorrente.service;

import com.pix.recorrente.domain.enums.EnumFrequencia;
import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import com.pix.recorrente.domain.enums.EnumStatusRisco;
import com.pix.recorrente.domain.model.Agendamento;
import com.pix.recorrente.domain.model.AnaliseFraude;
import com.pix.recorrente.dto.AgendamentoRequest;
import com.pix.recorrente.exception.AgendamentoDuplicadoException;
import com.pix.recorrente.exception.AgendamentoRejeitadoFraudeException;
import com.pix.recorrente.repository.AgendamentoRepository;
import com.pix.recorrente.service.mapper.AgendamentoMapper;
import com.pix.recorrente.service.serialization.AnaliseFraudeJsonSerializer;
import com.pix.recorrente.service.state.AgendamentoStatusTransitioner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private AntifraudeService antifraudeService;

    @Mock
    private AgendamentoStatusTransitioner statusTransitioner;

    @Mock
    private AgendamentoMapper agendamentoMapper;

    @Mock
    private AnaliseFraudeJsonSerializer analiseFraudeJsonSerializer;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private AgendamentoRequest validRequest;
    private String chaveIdempotencia;
    private Agendamento mockAgendamento;
    private AnaliseFraude approvedAnalysis;

    @BeforeEach
    void setup() {
        validRequest = new AgendamentoRequest(
                "cliente-123",
                "usuario@email.com",
                new BigDecimal("1000.00"),
                EnumFrequencia.MENSAL,
                LocalDate.now().plusDays(1),
                12
        );
        chaveIdempotencia = "idem-key-123";

        mockAgendamento = Agendamento.builder()
                .id(UUID.randomUUID())
                .clienteId("cliente-123")
                .chavePixRecebedor("usuario@email.com")
                .valor(new BigDecimal("1000.00"))
                .frequencia(EnumFrequencia.MENSAL)
                .dataInicio(LocalDate.now().plusDays(1))
                .quantidadeParcelas(12)
                .status(EnumStatusAgendamento.EM_ANALISE)
                .dataCriacao(LocalDateTime.now())
                .chaveIdempotencia(chaveIdempotencia)
                .build();

        approvedAnalysis = new AnaliseFraude(
                EnumStatusRisco.APROVADO,
                100,
                java.util.List.of(),
                LocalDateTime.now()
        );
    }

    @Test
    void testCreateAgendamento_Success_WhenApproved() {
        when(agendamentoRepository.findByChaveIdempotencia(chaveIdempotencia))
                .thenReturn(Optional.empty());
        when(antifraudeService.analisar("usuario@email.com", new BigDecimal("1000.00")))
                .thenReturn(approvedAnalysis);
        when(analiseFraudeJsonSerializer.serialize(approvedAnalysis))
                .thenReturn("{\"statusRisco\":\"APROVADO\"}");
        when(agendamentoMapper.toEntity(eq(validRequest), eq(chaveIdempotencia), anyString()))
                .thenReturn(mockAgendamento);

        Agendamento result = agendamentoService.criarAgendamento(validRequest, chaveIdempotencia);

        assertNotNull(result);
        assertEquals("cliente-123", result.getClienteId());
        verify(agendamentoRepository).saveAndFlush(mockAgendamento);
        verify(statusTransitioner).transicionar(mockAgendamento, EnumStatusRisco.APROVADO);
    }

    @Test
    void testCreateAgendamento_Rejected_WhenFraudDetected() {
        when(agendamentoRepository.findByChaveIdempotencia(chaveIdempotencia))
                .thenReturn(Optional.empty());

        AnaliseFraude rejectedAnalysis = new AnaliseFraude(
                EnumStatusRisco.REJEITADO,
                10,
                java.util.List.of("RNF-02: Chave Pix em blacklist"),
                LocalDateTime.now()
        );

        when(antifraudeService.analisar("usuario@email.com", new BigDecimal("1000.00")))
                .thenReturn(rejectedAnalysis);
        when(analiseFraudeJsonSerializer.serialize(rejectedAnalysis))
                .thenReturn("{\"statusRisco\":\"REJEITADO\"}");
        when(agendamentoMapper.toEntity(eq(validRequest), eq(chaveIdempotencia), anyString()))
                .thenReturn(mockAgendamento);

        assertThrows(AgendamentoRejeitadoFraudeException.class, () ->
                agendamentoService.criarAgendamento(validRequest, chaveIdempotencia)
        );

        verify(agendamentoRepository).saveAndFlush(mockAgendamento);
        verify(statusTransitioner).transicionar(mockAgendamento, EnumStatusRisco.REJEITADO);
    }

    @Test
    void testCreateAgendamento_RejectedStatusPersisted_WhenFraudDetected() {
        when(agendamentoRepository.findByChaveIdempotencia(chaveIdempotencia))
                .thenReturn(Optional.empty());

        AnaliseFraude rejectedAnalysis = new AnaliseFraude(
                EnumStatusRisco.REJEITADO,
                10,
                java.util.List.of("RNF-02"),
                LocalDateTime.now()
        );

        when(antifraudeService.analisar("usuario@email.com", new BigDecimal("1000.00")))
                .thenReturn(rejectedAnalysis);
        when(analiseFraudeJsonSerializer.serialize(rejectedAnalysis))
                .thenReturn("{\"statusRisco\":\"REJEITADO\"}");
        when(agendamentoMapper.toEntity(eq(validRequest), eq(chaveIdempotencia), anyString()))
                .thenReturn(mockAgendamento);

        try {
            agendamentoService.criarAgendamento(validRequest, chaveIdempotencia);
        } catch (AgendamentoRejeitadoFraudeException ignored) {
        }

        var inOrder = inOrder(agendamentoRepository, statusTransitioner);
        inOrder.verify(agendamentoRepository).saveAndFlush(mockAgendamento);
        inOrder.verify(statusTransitioner).transicionar(mockAgendamento, EnumStatusRisco.REJEITADO);
    }

    @Test
    void testCreateAgendamento_ManualReview_WhenStatusIsReviewNeeded() {
        when(agendamentoRepository.findByChaveIdempotencia(chaveIdempotencia))
                .thenReturn(Optional.empty());

        AnaliseFraude reviewAnalysis = new AnaliseFraude(
                EnumStatusRisco.REVISAO_MANUAL,
                40,
                java.util.List.of("RNF-01: Valor alto"),
                LocalDateTime.now()
        );

        when(antifraudeService.analisar("usuario@email.com", new BigDecimal("1000.00")))
                .thenReturn(reviewAnalysis);
        when(analiseFraudeJsonSerializer.serialize(reviewAnalysis))
                .thenReturn("{\"statusRisco\":\"REVISAO_MANUAL\"}");
        when(agendamentoMapper.toEntity(eq(validRequest), eq(chaveIdempotencia), anyString()))
                .thenReturn(mockAgendamento);

        Agendamento result = agendamentoService.criarAgendamento(validRequest, chaveIdempotencia);

        assertNotNull(result);
        verify(statusTransitioner).transicionar(mockAgendamento, EnumStatusRisco.REVISAO_MANUAL);
    }

    @Test
    void testCreateAgendamento_Duplicate_ThrowsException() {
        Agendamento existingAgendamento = Agendamento.builder()
                .id(UUID.randomUUID())
                .build();

        when(agendamentoRepository.findByChaveIdempotencia(chaveIdempotencia))
                .thenReturn(Optional.of(existingAgendamento));

        assertThrows(AgendamentoDuplicadoException.class, () ->
                agendamentoService.criarAgendamento(validRequest, chaveIdempotencia)
        );

        verify(antifraudeService, never()).analisar(anyString(), any());
    }

    @Test
    void testObtainAgendamento_ById_Success() {
        UUID agendamentoId = UUID.randomUUID();
        when(agendamentoRepository.findById(agendamentoId))
                .thenReturn(Optional.of(mockAgendamento));

        Agendamento result = agendamentoService.obterAgendamentoPorId(agendamentoId);

        assertNotNull(result);
        assertEquals("cliente-123", result.getClienteId());
    }

    @Test
    void testParseAnaliseFraude_Success() {
        String json = "{\"statusRisco\":\"APROVADO\",\"score\":100}";
        AnaliseFraude expected = new AnaliseFraude(
                EnumStatusRisco.APROVADO,
                100,
                java.util.List.of(),
                LocalDateTime.now()
        );

        when(analiseFraudeJsonSerializer.deserialize(json))
                .thenReturn(expected);

        AnaliseFraude result = agendamentoService.parseAnaliseFraude(json);

        assertNotNull(result);
        assertEquals(EnumStatusRisco.APROVADO, result.statusRisco());
    }
}
