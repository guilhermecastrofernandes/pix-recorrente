package com.pix.recorrente.domain.model;

import com.pix.recorrente.domain.enums.EnumFrequencia;
import com.pix.recorrente.domain.enums.EnumStatusAgendamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agendamentos")
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getChavePixRecebedor() { return chavePixRecebedor; }
    public void setChavePixRecebedor(String chavePixRecebedor) { this.chavePixRecebedor = chavePixRecebedor; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public EnumFrequencia getFrequencia() { return frequencia; }
    public void setFrequencia(EnumFrequencia frequencia) { this.frequencia = frequencia; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public Integer getQuantidadeParcelas() { return quantidadeParcelas; }
    public void setQuantidadeParcelas(Integer quantidadeParcelas) { this.quantidadeParcelas = quantidadeParcelas; }

    public EnumStatusAgendamento getStatus() { return status; }
    public void setStatus(EnumStatusAgendamento status) { this.status = status; }

    public String getAnaliseFraudeJson() { return analiseFraudeJson; }
    public void setAnaliseFraudeJson(String analiseFraudeJson) { this.analiseFraudeJson = analiseFraudeJson; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getChaveIdempotencia() { return chaveIdempotencia; }
    public void setChaveIdempotencia(String chaveIdempotencia) { this.chaveIdempotencia = chaveIdempotencia; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    // ===== Domain Methods (Business Logic) =====

    /** Transiciona status baseado em resultado de fraude. Encapsula regra de negócio. */
    public void transicionarParaStatus(EnumStatusAgendamento novoStatus) {
        if (novoStatus == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        // Validar transição válida (opcional: implementar state machine aqui)
        this.status = novoStatus;
    }

    /** Retorna se agendamento está ativo (pode gerar pagamentos). */
    public boolean estaAtivo() {
        return this.status == EnumStatusAgendamento.ATIVO;
    }

    /** Retorna se agendamento foi rejeitado por fraude. */
    public boolean foiRejeitadoPorFraude() {
        return this.status == EnumStatusAgendamento.REJEITADO_FRAUDE;
    }

    /** Retorna se agendamento está em análise manual. */
    public boolean estaEmAnalise() {
        return this.status == EnumStatusAgendamento.EM_ANALISE;
    }

    /** Retorna se o agendamento ainda possui parcelas a executar (simplificado). */
    public boolean possuiParcelasAExecutar() {
        return this.quantidadeParcelas == null || this.quantidadeParcelas > 0;
    }

    /** Retorna representação legível do agendamento. */
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
