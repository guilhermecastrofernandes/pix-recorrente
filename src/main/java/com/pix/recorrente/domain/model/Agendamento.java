package com.pix.recorrente.domain.model;

import com.pix.recorrente.domain.enums.EnumFrequencia;
import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String clienteId;

    @Column(nullable = false)
    private String chavePixRecebedor;

    @Column(nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumFrequencia frequencia;

    @Column(nullable = false)
    private LocalDate dataInicio;

    private Integer quantidadeParcelas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumStatusAgendamento status;

    @Column(columnDefinition = "TEXT")
    private String analiseFraudeJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false, unique = true)
    private String chaveIdempotencia;

    @Version
    private Long version;

    @Override
    public String toString() {
        return "Agendamento{" +
                "id=" + id +
                ", clienteId='" + clienteId + '\'' +
                ", chavePixRecebedor='" + chavePixRecebedor + '\'' +
                ", valor=" + valor +
                ", status=" + status +
                ", dataCriacao=" + dataCriacao +
                '}';
    }
}
