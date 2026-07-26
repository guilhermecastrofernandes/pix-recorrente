package com.pix.recorrente.domain.enums;

import java.time.LocalDate;

public enum EnumFrequencia {
    MENSAL {
        @Override
        public LocalDate proximaData(LocalDate base) {
            return base.plusMonths(1);
        }
    };

    public abstract LocalDate proximaData(LocalDate base);
}
