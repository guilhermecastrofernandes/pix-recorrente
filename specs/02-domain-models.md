# 02. Modelos de Domínio e Entidades

Este documento descreve as entidades principais do domínio do Pix Recorrente, seus atributos, enumerações de estado e relacionamentos.

---

## 1. Diagrama Entidade-Relacionamento (Conceitual)


+---------------------------------+        1:N        +---------------------------------+
|          Agendamento            |-------------------|       PagamentoRecorrente       |
+---------------------------------+                   +---------------------------------+
| id: UUID                        |                   | id: UUID                        |
| clienteId: String               |                   | agendamentoId: UUID             |
| chavePixRecebedor: String       |                   | valor: BigDecimal               |
| valorMensal: BigDecimal         |                   | dataPrevista: LocalDate         |
| frequencia: EnumFrequencia      |                   | status: EnumStatusPagamento     |
| status: EnumStatusAgendamento   |                   | chaveIdempotencia: String       |
| analiseFraude: AnaliseFraudeVO  |                   | dataExecucao: LocalDateTime     |
| dataCriacao: LocalDateTime      |                   +---------------------------------+
+---------------------------------+


---

## 2. Especificação das Entidades

### 2.1. Entidade: `Agendamento`
Representa o contrato da assinatura ou pagamento recorrente Pix configurado pelo usuário.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `UUID` | Identificador único do agendamento. |
| `clienteId` | `String` | Documento (CPF/CNPJ) ou ID do pagador. |
| `chavePixRecebedor` | `String` | Chave Pix do favorecido (E-mail, CPF/CNPJ, Telefone, EVP). |
| `valor` | `BigDecimal` | Valor nominal de cada parcela recorrente. |
| `frequencia` | `Enum (SEMANAL, MENSAL)` | Periodicidade de execução das cobranças. |
| `dataInicio` | `LocalDate` | Data da primeira cobrança. |
| `quantidadeParcelas` | `Integer` | Total de repetições (opcional / nulo para ilimitado). |
| `status` | `EnumStatusAgendamento` | Estado atual do contrato. |
| `resultadoFraude` | `AnaliseFraude` (Value Object) | Registro do resultado retornado pelo módulo antifraude. |
| `dataCriacao` | `LocalDateTime` | Timestamp de registro no sistema. |

---

### 2.2. Entidade: `PagamentoRecorrente`
Representa cada ocorrência/parcela individual de cobrança gerada a partir do agendamento.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `UUID` | Identificador único da transação individual. |
| `agendamentoId` | `UUID` | Chave estrangeira para o agendamento pai. |
| `valor` | `BigDecimal` | Valor efetivo da parcela. |
| `dataPrevista` | `LocalDate` | Data em que o pagamento deve ser liquidado. |
| `dataExecucao` | `LocalDateTime` | Timestamp real de liquidação na SPI/BACEN. |
| `status` | `EnumStatusPagamento` | Status da liquidação da parcela. |
| `chaveIdempotencia` | `String` | Hash/UUID para controle de duplicidade de liquidação. |
| `mensagemErro` | `String` | Detalhes em caso de falha no processamento/DLQ. |

---

### 2.3. Value Object: `AnaliseFraude`
Objeto imutável contendo o parecer do Módulo de Prevenção à Fraude.

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `statusRisco` | `EnumStatusRisco` | Resultado da avaliação (`APROVADO`, `REJEITADO`, `REVISAO_MANUAL`). |
| `score` | `Integer` | Pontuação numérica calculada (0 a 100). |
| `regrasVioladas` | `List<String>` | Lista com os códigos das regras disparadas (ex: `["RNF-01", "RNF-03"]`). |
| `dataAnalise` | `LocalDateTime` | Timestamp da execução da análise. |

---

## 3. Enumerações de Estado

```java
public enum EnumStatusAgendamento {
    ATIVO,              // Fraude APROVADA e contrato em vigência
    EM_ANALISE,         // Fraude em REVISAO_MANUAL
    REJEITADO_FRAUDE,   // Fraude REJEITADA
    CANCELADO           // Cancelado pelo cliente
}

public enum EnumStatusRisco {
    APROVADO,
    REJEITADO,
    REVISAO_MANUAL
}

public enum EnumStatusPagamento {
    PENDENTE,
    PROCESSANDO,
    SUCESSO,
    FALHA_PROCESSAMENTO,
    ENVIADO_DLQ
}