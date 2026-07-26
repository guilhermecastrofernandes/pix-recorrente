# API Pix Recorrente Seguro

PoC de API para agendamento de Pix recorrente com módulo antifraude, orquestração assíncrona via RabbitMQ e idempotência.

## Requisitos

- Java 21+
- Maven 3.9+
- Docker (opcional, para RabbitMQ)

## Build

```bash
./mvnw clean compile
./mvnw package -DskipTests
```

## Rodar Localmente

### Sem Docker (precisa RabbitMQ rodando em localhost:5672)

```bash
./mvnw spring-boot:run
```

App disponível em `http://localhost:8080`

### Com Docker Compose

```bash
docker compose up
```

App disponível em `http://localhost:8081`
RabbitMQ Management: `http://localhost:15672` (guest/guest)

## Endpoints

### POST /v1/agendamentos
Criar agendamento recorrente. Obrigatório header `X-Idempotency-Key`.

**Request:**
```json
{
  "clienteId": "12345678900",
  "chavePixRecebedor": "usuario@email.com",
  "valor": 150.00,
  "frequencia": "MENSAL",
  "dataInicio": "2026-08-01",
  "quantidadeParcelas": 12
}
```

**Responses:**
- `201 Created`: Agendamento ATIVO (aprovado)
- `202 Accepted`: Agendamento EM_ANALISE (revisão manual)
- `422 Unprocessable Entity`: Rejeitado (fraude detectada)

### GET /v1/agendamentos/{id}
Consultar agendamento + histórico de pagamentos.

**Response:**
- `200 OK`: Agendamento + pagamentos associados
- `404 Not Found`: ID não existe

## Antifraude

3 regras determinísticas:

1. **RNF-01**: Valor > R$ 5.000,00 → REVISAO_MANUAL
2. **RNF-02**: Chave Pix em blacklist → REJEITADO
3. **RNF-03**: 20h-06h + valor > R$ 1.000,00 → REVISAO_MANUAL

Score: 5 (aprovado), 50-65 (revisão), 95 (rejeitado)

## Arquitetura

- **Linguagem**: Java 21 + Spring Boot 3.3.2
- **DB**: H2 (em-memória, para PoC)
- **Messaging**: RabbitMQ + DLQ
- **Idempotência**: unique constraint + X-Idempotency-Key
- **Testes**: JUnit 5 + Mockito

## Estrutura

```
src/main/java/com/pix/recorrente/
├── domain/
│   ├── enums/ (Status, Risco, Pagamento, Frequencia)
│   └── model/ (Agendamento, PagamentoRecorrente, AnaliseFraude)
├── repository/ (JPA + custom queries)
├── service/
│   ├── fraud/ (Chain of Responsibility: RNF01, RNF02, RNF03)
│   ├── state/ (State Machine: AtivoState, EmAnaliseState, etc)
│   ├── serialization/ (AnaliseFraudeJsonSerializer - SRP)
│   ├── mapper/ (AgendamentoMapper - SRP)
│   ├── builder/ (AgendamentoResponseBuilder - SRP)
│   ├── execution/ (PagamentoExecutionScheduler + Service)
│   ├── orchestration/ (PagamentoRecorrenteOrchestrator - SRP)
│   └── core/ (AgendamentoService, AntifraudeService)
├── controller/ (REST)
├── exception/ (handlers)
├── messaging/ (RabbitMQ pub/sub + Converter - SRP)
└── config/ (RabbitMQ, @EnableScheduling)
```

## Testes

```bash
./mvnw test
```

## Docker

Build imagem:
```bash
docker build -t pix-recorrente-api:latest .
```

Run com compose (recomendado):
```bash
docker-compose up -d
docker-compose logs -f app
```

## H2 Console

Acesso: `http://localhost:8080/h2-console`

JDBC URL: `jdbc:h2:mem:testdb`
User: `sa`
Password: (vazio)

## Specs

- [01-requirements.md](specs/01-requirements.md)
- [02-domain-models.md](specs/02-domain-models.md)
- [03-endpoints.md](specs/03-endpoints.md)

## SLA

- Resposta POST: < 200ms (síncrono)
- Retry: 3x com exponential backoff
- Pagamentos: async via RabbitMQ

## Padrões de Design

- **Chain of Responsibility**: Regras de fraude (RNF-01, RNF-02, RNF-03) plugáveis
- **State Machine**: Transições de status explícitas (ATIVO, EM_ANALISE, REJEITADO)
- **Builder**: Construção de payload de orquestração
- **Strategy**: Implementações de estado (AtivoState, EmAnaliseState, etc)

## Princípios SOLID

- **SRP**: Serviços isolados (Serializer, Mapper, Builder, Orchestrator)
- **OCP**: Estados extensíveis via Strategy, regras plugáveis via Chain
- **DIP**: Abstrações em services, injeção de dependências

## Automação

- **Scheduler**: PagamentoExecutionScheduler roda a cada 60s
- **Execução**: PagamentoRecorrente com status PENDENTE é executado automaticamente
- **Resiliência**: Falha não bloqueia loop (continua próximos pagamentos)

## Roadmap Pós-PoC

**Fase 1:** Retry com exponential backoff + DLQ monitoring
**Fase 2:** Authentication (JWT/OAuth2) + Rate limiting  
**Fase 3:** Event-driven com Kafka + SPI/BACEN integration
**Fase 4:** Schema registry (Avro) + Auditoria

## Docs Detalhadas

- [04-case.md](specs/04-case.md) — Design patterns, SOLID, trade-offs
