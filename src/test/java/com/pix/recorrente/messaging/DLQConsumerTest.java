package com.pix.recorrente.messaging;

import com.pix.recorrente.service.alerta.AlertaPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DLQConsumerTest {

    @Mock
    private AlertaPort alertaPort;

    @InjectMocks
    private DLQConsumer dlqConsumer;

    private String capturarMensagem() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(alertaPort).alertar(eq(AlertaPort.TipoAlerta.MENSAGEM_DESCARTADA), captor.capture());
        return captor.getValue();
    }

    @Test
    void testConsumir_MensagemDescartada_EmiteAlerta() {
        String agendamentoId = UUID.randomUUID().toString();
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", agendamentoId);
        payload.put("chaveIdempotencia", "chave-abc");

        dlqConsumer.consumirMensagemDescartada(payload);

        String mensagem = capturarMensagem();
        assertTrue(mensagem.contains(agendamentoId));
        assertTrue(mensagem.contains("chave-abc"));
    }

    @Test
    void testConsumir_PayloadSemCamposEsperados_NaoQuebra() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("campoInesperado", "valor");

        assertDoesNotThrow(() -> dlqConsumer.consumirMensagemDescartada(payload));

        verify(alertaPort).alertar(eq(AlertaPort.TipoAlerta.MENSAGEM_DESCARTADA), any());
    }

    @Test
    void testConsumir_PayloadNulo_NaoQuebra() {
        assertDoesNotThrow(() -> dlqConsumer.consumirMensagemDescartada(null));

        verify(alertaPort).alertar(eq(AlertaPort.TipoAlerta.MENSAGEM_DESCARTADA), any());
    }
}
