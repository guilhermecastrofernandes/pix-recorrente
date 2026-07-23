package com.pix.recorrente.service.fraud;

import java.math.BigDecimal;

public interface FraudRule {
    void apply(FraudAnalysisContext context, String chavePixRecebedor, BigDecimal valor);
}
