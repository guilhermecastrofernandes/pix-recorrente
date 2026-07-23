package com.pix.recorrente.domain.model;

import com.pix.recorrente.domain.enums.EnumStatusPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagamentos_recorrentes")
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

    private String mensagemErro;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAgendamentoId() { return agendamentoId; }
    public void setAgendamentoId(UUID agendamentoId) { this.agendamentoId = agendamentoId; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public LocalDate getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(LocalDate dataPrevista) { this.dataPrevista = dataPrevista; }

    public LocalDateTime getDataExecucao() { return dataExecucao; }
    public void setDataExecucao(LocalDateTime dataExecucao) { this.dataExecucao = dataExecucao; }

    public EnumStatusPagamento getStatus() { return status; }
    public void setStatus(EnumStatusPagamento status) { this.status = status; }

    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }

    public String getMensagemErro() { return mensagemErro; }
    public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }

    // ===== Domain Methods (Business Logic) =====

    /** Retorna se pagamento está pendente de execução. */
    public boolean estaPendente() {
        return this.status == EnumStatusPagamento.PENDENTE;
    }

    /** Retorna se pagamento foi executado com sucesso. */
    public boolean foiExecutado() {
        return this.status == EnumStatusPagamento.SUCESSO;
    }

    /** Retorna se pagamento falhou (pode ser reprocessado). */
    public boolean falhou() {
        return this.status == EnumStatusPagamento.FALHA_PROCESSAMENTO;
    }

    /** Retorna se pagamento está sendo processado. */
    public boolean estaProcessando() {
        return this.status == EnumStatusPagamento.PROCESSANDO;
    }

    /** Retorna se pagamento está em DLQ (irrecuperável sem intervenção). */
    public boolean foiEnviadoAoDLQ() {
        return this.status == EnumStatusPagamento.ENVIADO_DLQ;
    }

    /** Marca pagamento como processado com sucesso. */
    public void marcarComoSucesso() {
        this.status = EnumStatusPagamento.SUCESSO;
        this.dataExecucao = LocalDateTime.now();
        this.mensagemErro = null;
    }

    /** Marca pagamento como falha com mensagem de erro. */
    public void marcarComoFalha(String mensagemErro) {
        this.status = EnumStatusPagamento.FALHA_PROCESSAMENTO;
        this.mensagemErro = mensagemErro;
    }

    @Override
    public String toString() {
        return "PagamentoRecorrente{" +
                "id=" + id +
                ", agendamentoId=" + agendamentoId +
                ", valor=" + valor +
                ", dataPrevista=" + dataPrevista +
                ", status=" + status +
                '}';
    }
}
