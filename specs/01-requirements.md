# 01. Requisitos do Sistema e Arquitetura

Este documento detalha os requisitos funcionais, não funcionais e as regras de negócio para a Prova de Conceito (PoC) da **API de Pix Recorrente Seguro**, focada em escalabilidade, prevenção à fraude e resiliência transacional.

---

## 1. Requisitos Funcionais (RF)

* **RF-01: Registrar Agendamento Recorrente (`POST /v1/agendamentos`)**
    * A API deve receber a solicitação de agendamento de Pix recorrente contendo os dados da conta pagadora, recebedora (chave Pix), valor, frequência (mensal, semanal), data de início e término/limite de ocorrências.
    * Antes da gravação e agendamento, o sistema deve submeter os dados ao **Módulo de Prevenção à Fraude**.
    * Caso o risco seja `APROVADO`, o agendamento é salvo com status `ATIVO` e postado na fila para orquestração.
    * Caso o risco seja `REJEITADO`, o agendamento é registrado como `REJEITADO_FRAUDE` e nenhuma cobrança é agendada.
    * Caso seja `REVISAO_MANUAL`, o agendamento é gravado com status `EM_ANALISE`.

* **RF-02: Consultar Status do Agendamento (`GET /v1/agendamentos/{id}`)**
    * Permite consultar os detalhes do agendamento, status atual, histórico das análises de risco e pagamentos executados/agendados.

* **RF-03: Análise Preventiva de Fraude**
    * Executar motor de regras determinístico pré-processamento para avaliar o risco da transação recorrente.

---

## 2. Regras de Negócio de Fraude (RNF)

O Módulo de Prevenção à Fraude executa sequencialmente 3 regras principais de análise de risco:

1. **RNF-01: Limite de Valor por Recorrência (Valor Suspeito)**
    * **Regra:** Se o valor individual da recorrência for maior que **R$ 5.000,00**, a transação exige validação adicional.
    * **Ação:** Retorna `REVISAO_MANUAL`.

2. **RNF-02: Bloqueio de Chave Pix em Blacklist Nacional**
    * **Regra:** Se a chave Pix do recebedor constar na base de simulação de chaves suspeitas/denunciadas por fraude.
    * **Ação:** Retorna `REJEITADO`.

3. **RNF-03: Frequência e Janela Horária Noturna**
    * **Regra:** Se o agendamento for solicitado em janela noturna (20h às 06h) com valor superior a **R$ 1.000,00**.
    * **Ação:** Retorna `REVISAO_MANUAL`.

---

## 3. Requisitos Não Funcionais (RNF)

* **RNF-01: Resiliência e Trata de Falhas (Fila / DLQ)**
    * A comunicação e o processamento de execução das parcelas recorrentes devem ser assíncronos via broker de mensagens (RabbitMQ / Kafka).
    * Em caso de falhas transitórias (ex: timeout de conexão ou indisponibilidade de DICT/SPI), a mensagem deve tentar reprocessamento até 3 vezes com *Exponential Backoff*.
    * Persistindo o erro, a mensagem deve ser movida para uma **Dead Letter Queue (DLQ)** para isolamento e alerta.

* **RNF-02: Idempotência**
    * O endpoint de criação e o consumidor de pagamentos devem implementar chave de idempotência (`X-Idempotency-Key` ou `UUID` único de transação) para evitar cobranças duplicadas em cenários de *retries*.

* **RNF-03: Desempenho e Latência**
    * O tempo de resposta para a criação e avaliação do agendamento (`POST /v1/agendamentos`) não deve exceder 200ms (SLA síncrono).

---

## 4. Decisões Arquiteturais e Trade-Offs

| Decisão | Opção Escolhida                               | Justificativa / Trade-Off |
| :--- |:----------------------------------------------| :--- |
| **Linguagem & Framework** | Java com a versão mais nova Spring Boot 3.3.x | Tipagem forte, ecossistema maduro para ecossistemas bancários, facilidade de integração com Spring AMQP/Kafka. |
| **Messaging / Broker** | RabbitMQ                       | **RabbitMQ:** Excelente suporte nativo a filas de trabalho (*work queues*), roteamento por *exchanges* e facilidade de DLQ nativa.<br>**Trade-off:** Não suporta *replay* de mensagens como o Kafka, porém reduz complexidade de operação. |
| **Processamento de Fraude** | Síncrono no Aceite                            | A validação do aceite ocorre na requisição inicial para feedback imediato ao cliente, transferindo apenas a execução diferida das parcelas para a fila assíncrona. |
