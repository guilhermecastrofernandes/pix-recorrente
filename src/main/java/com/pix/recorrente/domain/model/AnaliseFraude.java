package com.pix.recorrente.domain.model;

import com.pix.recorrente.domain.enums.EnumStatusRisco;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record AnaliseFraude(
        EnumStatusRisco statusRisco,
        Integer score,
        List<String> regrasVioladas,
        LocalDateTime dataAnalise
) {
    public AnaliseFraude {
        regrasVioladas = Objects.requireNonNullElse(regrasVioladas, new ArrayList<>());
    }
}
