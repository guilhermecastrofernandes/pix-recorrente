package com.pix.recorrente.service.alerta;


public interface AlertaPort {

    void alertar(TipoAlerta tipo, String mensagem);

    enum TipoAlerta {

        PAGAMENTO_EM_DLQ,

        MENSAGEM_DESCARTADA
    }
}
