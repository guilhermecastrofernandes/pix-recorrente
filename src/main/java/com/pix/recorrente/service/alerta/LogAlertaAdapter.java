package com.pix.recorrente.service.alerta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class LogAlertaAdapter implements AlertaPort {
    private static final Logger logger = LoggerFactory.getLogger(LogAlertaAdapter.class);

    @Override
    public void alertar(TipoAlerta tipo, String mensagem) {
        logger.error("[ALERTA][{}] {}", tipo, mensagem);
    }
}
