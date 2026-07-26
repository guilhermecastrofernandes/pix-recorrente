package com.pix.recorrente.domain.model;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamentos_recorrentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoRecorrente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID agendamentoId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataPrevista;

    private LocalDateTime dataExecucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumStatusPagamento status;

    @Column(nullable = false, unique = true)
    private String chaveIdempotencia;

    @Column(nullable = false)
    private Integer numeroParcela;

    private String mensagemErro;

    @Default
    private Integer tentativas = 0;

    private LocalDateTime proximaExecucao;

    private LocalDateTime alertadoEm;

    public void marcarComoAlertado(LocalDateTime quando) {
        this.alertadoEm = quando;
    }

    public void marcarComoSucesso() {
        this.status = EnumStatusPagamento.SUCESSO;
        this.dataExecucao = LocalDateTime.now();
        this.mensagemErro = null;
    }

    public void marcarComoFalha(String mensagemErro) {
        this.status = EnumStatusPagamento.FALHA_PROCESSAMENTO;
        this.mensagemErro = mensagemErro;
        this.tentativas = (this.tentativas != null ? this.tentativas : 0) + 1;
        long delaySeconds = (long) Math.pow(2, this.tentativas);
        this.proximaExecucao = LocalDateTime.now().plusSeconds(delaySeconds);
    }

    @Override
    public String toString() {
        return "PagamentoRecorrente{" +
                "id=" + id +
                ", agendamentoId=" + agendamentoId +
                ", valor=" + valor +
                ", dataPrevista=" + dataPrevista +
                ", numeroParcela=" + numeroParcela +
                ", status=" + status +
                '}';
    }
}
