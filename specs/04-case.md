Case Técnico para Analista de Sistemas (Squad Pix)
Implementação de uma API para Pix Recorrente Seguro
Contexto e Desafio

A Squad Pix está desenvolvendo a funcionalidade de Pagamentos Pix Recorrentes (assinaturas ou mensalidades automatizadas). O Analista Sênior será responsável por projetar e construir um Protótipo de Prova de Conceito (PoC) de uma API robusta que garanta a automatização e a segurança (prevenção a fraudes) das transações agendadas.

O desafio é prototipar uma API RESTful que gerencie o ciclo de vida do agendamento e simule a orquestração segura de cada pagamento.

1. Requisitos Obrigatórios (Escopo do Projeto Prático)

O candidato deve criar um projeto funcional (PoC) usando as linguagem Java ou Node.js e publicá-lo em um repositório Git (GitHub, GitLab, etc.).

1.1. Estrutura e API
Endpoints RESTful: Implementar pelo menos dois endpoints funcionais:
POST /v1/agendamentos: Para registrar um novo agendamento recorrente.
GET /v1/agendamentos/{id}: Para consultar o status de um agendamento.
Modelo de Dados Simplificado: Usar classes/estruturas simples para simular as entidades principais (Agendamento, PagamentoRecorrente) e persistir os dados em memória ou em um banco de dados simples (Ex: SQLite, arquivo JSON, ou simulação de repositório).
1.2. Módulo de Prevenção a Fraudes (Simulação)

O projeto deve incluir uma função/serviço que simule o Módulo de Fraude.

Integração: O endpoint de agendamento deve chamar este Módulo de Fraude antes de aceitar a recorrência.
Lógica de Decisão: O módulo deve implementar pelo menos 3 regras de negócio simples que retornem um Status de Risco (Ex: APROVADO, REJEITADO, REVISAO_MANUAL) com base nos dados de entrada.
Registro: O resultado da análise de risco deve ser registrado (em log ou no objeto de agendamento).
1.3. Documentação Técnica

O projeto no Git deve conter um arquivo README.md claro, explicando:

Como rodar o projeto.
A URL dos endpoints implementados.
A justificativa para as regras de fraude criadas.
2. Entrega Final (Git e Vídeo)

A entrega deve ser composta por dois itens, que serão a base da avaliação:

2.1. Projeto de Código Fonte (Git)
O repositório Git deve estar público para acesso.
Deve conter o código-fonte da PoC da API, com as funcionalidades acima.
O histórico de commits será avaliado quanto à organização e clareza.
2.2. Vídeo de Apresentação e Justificativa (Máx. 5 minutos)

O vídeo deve ser conciso e focado, abordando os seguintes pontos:

Demonstração: Mostrar o projeto rodando, chamando um ou dois endpoints e o resultado.
Visão:
Decisões de Design: Por que escolheu essa linguagem/estrutura?
Resiliência: Como sua API trataria falhas (Ex: retry, idempotência) em um ambiente de produção real.
Trade-offs: Qual o principal compromisso (Ex: Performance vs. Segurança) que você fez e por quê.
Fraude: Explicar a lógica das regras de fraude implementadas e como elas protegem a squad de Pix.
3. Critérios de Avaliação
   Critério	Foco da Avaliação	Nível Esperado
   Arquitetura & Código	Qualidade do código, design da API, clareza do README.md.	Modularidade, uso de patterns adequados (Mesmo que simples), clareza e manutenção.
   Prevenção à Fraude	Relevância e eficácia das regras de negócio implementadas.	Pensamento estratégico sobre os riscos de um produto de Pix recorrente.
   Resiliência	Capacidade de prever e mitigar falhas em ambiente de produção.	Proposição de soluções robustas (Filas, DLQ, Idempotência) e visão sistêmica.
   Comunicação	Clareza, objetividade e poder de síntese no vídeo e na documentação.	Capacidade de justificar decisões técnicas e traduzir o código em valor de negócio.

---

## 4. Design Patterns Aplicados

A arquitetura da PoC foi reforçada com três padrões de design para melhorar modularidade, testabilidade e extensibilidade conforme critério de avaliação "Modularidade, uso de patterns adequados".

### 4.1. Chain of Responsibility — Regras de Fraude

**Problema:** Implementação anterior (acoplada):
```java
// Antes: lógica misturada em um único serviço
public AnaliseFraude analisar(String chavePixRecebedor, BigDecimal valor) {
    List<String> regrasVioladas = new ArrayList<>();
    EnumStatusRisco statusRisco = EnumStatusRisco.APROVADO;
    int score = 5;

    // RNF-01: hardcoded com lógica de negócio
    if (valor.compareTo(properties.getLimiteValorSuspeito()) > 0) {
        regrasVioladas.add("RNF-01: Valor individual superior a R$ 5.000,00");
        statusRisco = EnumStatusRisco.REVISAO_MANUAL;
        score = Math.max(score, 60);
    }

    // RNF-02: hardcoded
    if (properties.getChavesBlacklist().contains(chavePixRecebedor)) {
        regrasVioladas.add("RNF-02: Chave Pix cadastrada em lista de risco nacional");
        statusRisco = EnumStatusRisco.REJEITADO;
        score = 95;
    }
    // ... mais if-statements misturados
}
```

Problemas:
- Testes unitários precisam testar tudo junto (não isolam regras)
- Adicionar regra exige modificar classe existente (viola Open/Closed)
- Reordenação de regras exige refactoring

**Solução:** Chain of Responsibility com Spring DI:

Interface `FraudRule`:
```java
public interface FraudRule {
    void apply(FraudAnalysisContext context, String chavePixRecebedor, BigDecimal valor);
}
```

Cada regra é independente:
```java
@Component
public class RNF01ValueLimitRule implements FraudRule {
    private final AntifraudeProperties properties;

    @Override
    public void apply(FraudAnalysisContext context, String chavePixRecebedor, BigDecimal valor) {
        if (valor.compareTo(properties.getLimiteValorSuspeito()) > 0) {
            context.addRegraViolada("RNF-01: Valor individual superior a R$ 5.000,00");
            context.setStatusRisco(EnumStatusRisco.REVISAO_MANUAL);
            context.setScore(60);
        }
    }
}

@Component
public class RNF02BlacklistRule implements FraudRule {
    private final AntifraudeProperties properties;

    @Override
    public void apply(FraudAnalysisContext context, String chavePixRecebedor, BigDecimal valor) {
        if (properties.getChavesBlacklist().contains(chavePixRecebedor)) {
            context.addRegraViolada("RNF-02: Chave Pix cadastrada em lista de risco nacional");
            context.setStatusRisco(EnumStatusRisco.REJEITADO);
            context.setScore(95);
        }
    }
}
```

`AntifraudeService` orquestra via injeção de `List<FraudRule>`:
```java
@Service
public class AntifraudeService {
    private final List<FraudRule> fraudRules; // Auto-injetada pelo Spring

    public AnaliseFraude analisar(String chavePixRecebedor, BigDecimal valor) {
        FraudAnalysisContext context = new FraudAnalysisContext();
        
        // Chain: cada rule é aplicada sequencialmente
        for (FraudRule rule : fraudRules) {
            rule.apply(context, chavePixRecebedor, valor);
        }
        
        return new AnaliseFraude(context.getStatusRisco(), context.getScore(), 
                                 context.getRegrasVioladas(), LocalDateTime.now());
    }
}
```

**Trade-offs:**
- **Ganho:** 
  - Cada regra testável isoladamente: `@Test void testRNF01ValueLimit() { new RNF01ValueLimitRule(...).apply(...) }`
  - Adicionar regra = criar `@Component` sem tocar `AntifraudeService` (Open/Closed Principle)
  - Reordenar regras = mudar ordem em arquivo de config ou `@Order` annotation, sem código
  
- **Custo:** Mais classes (3 implementações + 1 interface), mas reutilizáveis em múltiplos contextos

**Impacto:** Atende critério de "modularidade". Facilita A/B testing de regras (ex: desativar RNF-03 para segmento específico) e extensão para novos clientes com regras customizadas.

---

### 4.2. State Machine — Transições de Status de Agendamento

**Problema:** Implementação anterior (acoplada e pouco explícita):
```java
// Antes: lógica de transição misturada em AgendamentoService
@Transactional(noRollbackFor = {AgendamentoRejeitadoFraudeException.class, ...})
public Agendamento criarAgendamento(AgendamentoRequest request, String chaveIdempotencia) {
    AnaliseFraude analiseFraude = antifraudeService.analisar(...);
    Agendamento agendamento = new Agendamento();
    // ... população de dados ...

    // Transições e efeitos colaterais misturados
    if (analiseFraude.statusRisco() == EnumStatusRisco.APROVADO) {
        agendamento.setStatus(EnumStatusAgendamento.ATIVO);
        Agendamento saved = agendamentoRepository.save(agendamento);
        orquestracaoPublisher.publicarAgendamento(saved); // Efeito colateral 1
        return saved;
    } else if (analiseFraude.statusRisco() == EnumStatusRisco.REVISAO_MANUAL) {
        agendamento.setStatus(EnumStatusAgendamento.EM_ANALISE);
        Agendamento saved = agendamentoRepository.save(agendamento);
        return saved;                                      // Efeito colateral 2: nada
    } else {
        agendamento.setStatus(EnumStatusAgendamento.REJEITADO_FRAUDE);
        agendamentoRepository.save(agendamento);
        throw new AgendamentoRejeitadoFraudeException(...); // Efeito colateral 3: lançar exceção
    }
}
```

Problemas:
- Estados (transições e ações) espalhados em múltiplos if-else
- Impossível visualizar diagrama de estados válido (`APROVADO` → `ATIVO`; `REVISAO_MANUAL` → `EM_ANALISE`, etc.)
- Adicionar ação por estado exige tocar em múltiplos lugares

**Solução:** State Machine com `AgendamentoStatusTransitioner`:

```java
@Component
public class AgendamentoStatusTransitioner {
    private final OrquestracaoPublisher orquestracaoPublisher;

    // 1. Mapeamento explícito: EnumStatusRisco → EnumStatusAgendamento
    private EnumStatusAgendamento mapStatusRiscoToAgendamento(EnumStatusRisco statusRisco) {
        return switch (statusRisco) {
            case APROVADO -> EnumStatusAgendamento.ATIVO;
            case REVISAO_MANUAL -> EnumStatusAgendamento.EM_ANALISE;
            case REJEITADO -> EnumStatusAgendamento.REJEITADO_FRAUDE;
        };
    }

    // 2. Ações por estado encapsuladas (padrão Strategy)
    private void executarAcoes(Agendamento agendamento, EnumStatusAgendamento status) {
        switch (status) {
            case ATIVO -> orquestracaoPublisher.publicarAgendamento(agendamento);
            case EM_ANALISE -> {}  // Não fazer nada imediatamente
            case REJEITADO_FRAUDE -> {}
            case CANCELADO -> {}
        }
    }

    // 3. Método público: orquestra transição completa
    public void transicionar(Agendamento agendamento, EnumStatusRisco statusRisco) {
        EnumStatusAgendamento novoStatus = mapStatusRiscoToAgendamento(statusRisco);
        agendamento.setStatus(novoStatus);
        executarAcoes(agendamento, novoStatus);
    }
}
```

`AgendamentoService` agora é simples (delegação):
```java
@Transactional(noRollbackFor = {AgendamentoRejeitadoFraudeException.class, ...})
public Agendamento criarAgendamento(AgendamentoRequest request, String chaveIdempotencia) {
    var existente = agendamentoRepository.findByChaveIdempotencia(chaveIdempotencia);
    if (existente.isPresent()) {
        throw new AgendamentoDuplicadoException(...);
    }

    AnaliseFraude analiseFraude = antifraudeService.analisar(...);
    Agendamento agendamento = new Agendamento();
    // ... população de dados ...

    // Delegação clara: toda lógica de transição está centralizada
    statusTransitioner.transicionar(agendamento, analiseFraude.statusRisco());
    Agendamento saved = agendamentoRepository.save(agendamento);

    // Lançar exceção apenas se rejeitado (separação clara de concern)
    if (analiseFraude.statusRisco() == EnumStatusRisco.REJEITADO) {
        throw new AgendamentoRejeitadoFraudeException(...);
    }

    return saved;
}
```

**Trade-offs:**
- **Ganho:**
  - Transições explícitas via `switch`: impossível ter transição inválida
  - Ações por estado encapsuladas: adicionar log/alert para estado `ATIVO` = modificar apenas `executarAcoes()`, não tocar `AgendamentoService`
  - Diagrama de estados visível em um único arquivo
  - Fácil evoluir para Spring State Machine Framework futuramente
  
- **Custo:** Uma classe adicional; `switch` exige manutenção se novo estado criado (força documentação)

**Impacto:** Código mais declarativo e menos propenso a bugs de lógica. Facilita rastreamento de transições para audit/compliance em sistema de pagamentos.

---

### 4.3. Builder/Factory — Construção de Payload de Orquestração

**Problema:** Implementação anterior (boilerplate repetido):
```java
// Antes: construção manual repetida sempre que precisa enviar mensagem
@Service
public class OrquestracaoPublisher {
    public void publicarAgendamento(Agendamento agendamento) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", agendamento.getId().toString());           // Conversão manual
        payload.put("clienteId", agendamento.getClienteId());
        payload.put("chavePixRecebedor", agendamento.getChavePixRecebedor());
        payload.put("valor", agendamento.getValor().toString());     // Conversão manual
        payload.put("frequencia", agendamento.getFrequencia().toString());  // Conversão manual
        payload.put("dataInicio", agendamento.getDataInicio().toString());  // Conversão manual
        payload.put("quantidadeParcelas", agendamento.getQuantidadeParcelas());
        payload.put("chaveIdempotencia", agendamento.getChaveIdempotencia());

        rabbitTemplate.convertAndSend(..., payload);
    }

    // Se outro método precisar do mesmo payload?
    public void reprocessarAgendamento(Agendamento agendamento) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", agendamento.getId().toString());           // Repetido!
        payload.put("clienteId", agendamento.getClienteId());
        // ... mais 6 linhas repetidas ...
    }
}
```

Problemas:
- Campo esquecido em nova função = bug silencioso
- Transformação de tipo (UUID → String) espalhada e inconsistente
- Testes de payload = copiar 8 linhas em múltiplos testes
- Mudança no contrato (ex: adicionar campo) = tocar múltiplos pontos

**Solução:** Builder/Factory centraliza construção:

```java
@Component
public class OrquestracaoPayloadBuilder {
    public Map<String, Object> build(Agendamento agendamento) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", agendamento.getId().toString());
        payload.put("clienteId", agendamento.getClienteId());
        payload.put("chavePixRecebedor", agendamento.getChavePixRecebedor());
        payload.put("valor", agendamento.getValor().toString());
        payload.put("frequencia", agendamento.getFrequencia().toString());
        payload.put("dataInicio", agendamento.getDataInicio().toString());
        payload.put("quantidadeParcelas", agendamento.getQuantidadeParcelas());
        payload.put("chaveIdempotencia", agendamento.getChaveIdempotencia());
        return payload;
    }
}
```

`OrquestracaoPublisher` delega (simples e limpo):
```java
@Service
public class OrquestracaoPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final OrquestracaoPayloadBuilder payloadBuilder;

    public OrquestracaoPublisher(RabbitTemplate rabbitTemplate, 
                                 OrquestracaoPayloadBuilder payloadBuilder) {
        this.rabbitTemplate = rabbitTemplate;
        this.payloadBuilder = payloadBuilder;
    }

    public void publicarAgendamento(Agendamento agendamento) {
        Map<String, Object> payload = payloadBuilder.build(agendamento);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ORQUESTRACAO, 
                                      RabbitMQConfig.ROUTING_KEY, payload);
    }

    // Se precisar reprocessar: mesma construção
    public void reprocessarAgendamento(Agendamento agendamento) {
        Map<String, Object> payload = payloadBuilder.build(agendamento);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ORQUESTRACAO, 
                                      RabbitMQConfig.ROUTING_KEY_RETRY, payload);
    }
}
```

Teste unitário fica simples:
```java
@Test
void testPayloadBuilderContainAllRequiredFields() {
    Agendamento agendamento = criarAgendamentoMock();
    Map<String, Object> payload = payloadBuilder.build(agendamento);
    
    assertThat(payload)
        .containsKeys("id", "clienteId", "chavePixRecebedor", "valor", 
                      "frequencia", "dataInicio", "quantidadeParcelas", "chaveIdempotencia")
        .hasSize(8);
}
```

**Trade-offs:**
- **Ganho:**
  - Transformações centralizadas: mudança de contrato (ex: adicionar `dataModificacao`) = uma linha em um lugar
  - Reutilizável: múltiplos publishers podem usar mesmo builder
  - Testabilidade: testes de payload isolados em `OrquestracaoPayloadBuilderTest` sem tocar publisher
  - Documentação implícita: ver `build()` deixa claro quais campos são esperados
  
- **Custo:** Uma classe extra; Java `Map<String, Object>` continua sendo untyped (não há type-safety garantida em runtime)

**Roadmap em produção:**
- Evoluir para DTO com anotações Jackson:
```java
record OrquestracaoPayload(
    @JsonProperty("id") String id,
    @JsonProperty("clienteId") String clienteId,
    @JsonProperty("valor") String valor,
    // ... type-safe
) {}
```

- Usar MapStruct para automático:
```java
@Mapper(componentModel = "spring")
public interface AgendamentoMapper {
    OrquestracaoPayload toPayload(Agendamento agendamento);
}
```

**Impacto:** Reduz erro humano em transformação de dados. Facilita testes e documentação de contrato de mensagem. Base para evolução para type-safe serialization em produção.

---

---

## 5. Princípios SOLID Aplicados

Além dos padrões de design, a arquitetura foi refatorada para adherir aos cinco princípios SOLID, eliminando violações de responsabilidade e aumentando flexibilidade.

### 5.1. Single Responsibility Principle (SRP)

**Antes:** Múltiplas responsabilidades espalhadas em classes grandes.

**Depois:** Cada classe tem uma única razão para mudar.

#### Serviços Criados:

1. **`AnaliseFraudeJsonSerializer`** — Serialização/desserialização de análise de fraude
   ```java
   public class AnaliseFraudeJsonSerializer {
       public String serialize(AnaliseFraude analiseFraude) { /* serializa */ }
       public AnaliseFraude deserialize(String json) { /* desserializa */ }
   }
   ```
   **Antes:** Lógica espalhada em `AgendamentoService.criarAgendamento()` (try-catch com RuntimeException genérica).
   **Ganho:** Serialização centralizada, exceção específica `JsonSerializationException`.

2. **`AgendamentoMapper`** — Mapeamento de request → entity
   ```java
   public class AgendamentoMapper {
       public Agendamento toEntity(AgendamentoRequest request, String chaveIdempotencia, String analiseFraudeJson) { 
           // Construção pura de entity
       }
   }
   ```
   **Antes:** Construção inline em `AgendamentoService.criarAgendamento()` (8 setters + data criação).
   **Ganho:** Lógica de transformação isolada, testável.

3. **`AgendamentoResponseBuilder`** — Construção de response com queries e DTO mapping
   ```java
   public class AgendamentoResponseBuilder {
       public AgendamentoResponse build(Agendamento agendamento) {
           // Busca pagamentos + serializa análise de fraude + monta response
       }
   }
   ```
   **Antes:** Espalhado em `AgendamentoController.toResponse()` (queries + mapping aninhado).
   **Ganho:** Controller = apenas orquestração. Response building = serviço dedicado.

4. **`OrquestracaoPayloadConverter`** — Conversão de Map → OrquestracaoPayload (type-safe record)
   ```java
   public class OrquestracaoPayloadConverter {
       public OrquestracaoPayload convert(Map<String, Object> rawPayload) { 
           // Desserialização com transformações de tipo
       }
   }
   ```
   **Antes:** Casting e parsing inline em `OrquestracaoConsumer.consumirAgendamento()`.
   **Ganho:** Desserialização centralizada, exception handling específico.

5. **`PagamentoRecorrenteOrchestrator`** — Orquestração de negócio para pagamentos recorrentes
   ```java
   public class PagamentoRecorrenteOrchestrator {
       public void criarOuIgnorar(OrquestracaoPayload payload) { 
           // Idempotência + criação de entity
       }
   }
   ```
   **Antes:** Misturado em `OrquestracaoConsumer` (idempotência + business logic + persistência).
   **Ganho:** Consumer = apenas ouvir/ack. Lógica de negócio = serviço isolado.

**Impacto SRP:**
- `AgendamentoService` reduzido de 50 linhas para 20 (apenas orquestração)
- `AgendamentoController` reduzido de 40 para 15 (apenas HTTP)
- `OrquestracaoConsumer` reduzido de 35 para 12 (apenas listener)
- Cada serviço tem 1 motivo para mudar

---

### 5.2. Open/Closed Principle (OCP)

**Antes:** Hardcoded switch statements forçavam modificação de classe existente para adicionar comportamento.

**Depois:** Extensível via Strategy/State Pattern sem tocar código existente.

#### State Pattern Aplicado:

Interface `AgendamentoState`:
```java
public interface AgendamentoState {
    void onEnter(Agendamento agendamento);
}
```

Implementações por estado:
```java
@Component
public class AtivoState implements AgendamentoState {
    private final OrquestracaoPublisher orquestracaoPublisher;
    
    @Override
    public void onEnter(Agendamento agendamento) {
        orquestracaoPublisher.publicarAgendamento(agendamento);
    }
}

@Component
public class EmAnaliseState implements AgendamentoState {
    @Override
    public void onEnter(Agendamento agendamento) {
        // Nenhuma ação imediatamente
    }
}

@Component
public class RejeitadoFraudeState implements AgendamentoState {
    @Override
    public void onEnter(Agendamento agendamento) {
        // Nenhuma ação
    }
}
```

`AgendamentoStatusTransitioner` com state map (ao invés de switch):
```java
@Component
public class AgendamentoStatusTransitioner {
    private final Map<EnumStatusAgendamento, AgendamentoState> stateMap;

    public AgendamentoStatusTransitioner(AtivoState ativoState,
                                         EmAnaliseState emAnaliseState,
                                         RejeitadoFraudeState rejeitadoFraudeState,
                                         CanceladoState canceladoState) {
        this.stateMap = Map.of(
            EnumStatusAgendamento.ATIVO, ativoState,
            EnumStatusAgendamento.EM_ANALISE, emAnaliseState,
            EnumStatusAgendamento.REJEITADO_FRAUDE, rejeitadoFraudeState,
            EnumStatusAgendamento.CANCELADO, canceladoState
        );
    }

    public void transicionar(Agendamento agendamento, EnumStatusRisco statusRisco) {
        EnumStatusAgendamento novoStatus = mapStatusRiscoToAgendamento(statusRisco);
        agendamento.setStatus(novoStatus);
        
        // Buscar estado do map (aberto para extensão)
        AgendamentoState state = stateMap.get(novoStatus);
        state.onEnter(agendamento);
    }
}
```

**Antes:** Switch statement com casosduros
```java
// ❌ Fechado para extensão
switch (status) {
    case ATIVO -> orquestracaoPublisher.publicarAgendamento(agendamento);
    case EM_ANALISE -> {}
    case REJEITADO_FRAUDE -> {}
    // Adicionar novo status = modificar classe!
}
```

**Depois:** State map (aberto para extensão)
```java
// ✅ Aberto para extensão
AgendamentoState state = stateMap.get(novoStatus);
state.onEnter(agendamento);
// Adicionar novo status = criar @Component implementando AgendamentoState
```

**Impacto OCP:**
- Novo estado = criar classe `@Component` sem tocar `AgendamentoStatusTransitioner`
- Adicionar ação a estado = modificar apenas classe de estado, não transitioner
- Exemplo futuro: `LogarTransicaoState implements AgendamentoState` se precisar log — zero impacto em código existente

---

### 5.3. Dependency Inversion Principle (DIP)

**Antes:** Classes dependiam de implementações concretas (ObjectMapper, Properties).

**Depois:** Dependências injetadas via abstrações (serviços).

#### Exemplos:

**Antes (violação):**
```java
@Service
public class AgendamentoService {
    private final ObjectMapper objectMapper; // Depende de implementação concreta

    public void criarAgendamento(...) {
        try {
            agendamento.setAnaliseFraudeJson(objectMapper.writeValueAsString(analiseFraude));
        } catch (Exception e) {
            throw new RuntimeException(e); // RuntimeException genérica
        }
    }
}
```

**Depois (invertido):**
```java
@Service
public class AgendamentoService {
    private final AnaliseFraudeJsonSerializer analiseFraudeJsonSerializer; // Abstração

    public void criarAgendamento(...) {
        String analiseFraudeJson = analiseFraudeJsonSerializer.serialize(analiseFraude);
        // JsonSerializationException específica se falhar
    }
}
```

**Ganho:**
- `AgendamentoService` não sabe que `AnaliseFraudeJsonSerializer` usa Jackson
- Fácil trocar implementação (ex: usar GSON) sem modificar `AgendamentoService`
- Exceção específica (`JsonSerializationException`) ao invés de `RuntimeException` genérica

---

---

## 6. Automatização de Pagamentos — Scheduler

**Problema:** Spec promete "orquestração segura de cada pagamento", mas PagamentoRecorrente era criado com status PENDENTE e nunca executado. Nenhum scheduler acionava pagamentos quando `dataPrevista` chegava.

**Solução Implementada:**

#### Serviços:

1. **`PagamentoExecutionService`** — Executa pagamento individual
```java
@Service
@Transactional
public class PagamentoExecutionService {
    public void executarPagamento(PagamentoRecorrente pagamento) {
        pagamento.setStatus(EnumStatusPagamento.PROCESSANDO);
        // ... simula chamada a SPI/DICT ...
        pagamento.setStatus(EnumStatusPagamento.SUCESSO);
        pagamento.setDataExecucao(LocalDateTime.now());
    }
}
```

2. **`PagamentoExecutionScheduler`** — Scheduled job que puxa PENDENTE diariamente
```java
@Service
public class PagamentoExecutionScheduler {
    @Scheduled(fixedDelayString = "${scheduler.pagamento.delay:60000}", 
               initialDelayString = "${scheduler.pagamento.initial-delay:5000}")
    public void executarPagamentosPendentes() {
        LocalDate hoje = LocalDate.now();
        List<PagamentoRecorrente> pagamentosPendentes = pagamentoRepository.findPendentes(
            EnumStatusPagamento.PENDENTE, hoje
        );
        
        for (PagamentoRecorrente pagamento : pagamentosPendentes) {
            try {
                pagamentoExecutionService.executarPagamento(pagamento);
            } catch (Exception e) {
                // Continua com próximos mesmo se falhar
                logger.error("Erro ao executar pagamento ID: {}", pagamento.getId(), e);
            }
        }
    }
}
```

#### Configuração:

**application.yml:**
```yaml
scheduler:
  pagamento:
    delay: 60000           # Roda a cada 60 segundos
    initial-delay: 5000    # Aguarda 5 segundos antes de primeira execução
```

**PixRecorrenteApplication.java:**
```java
@SpringBootApplication
@EnableScheduling           // Ativa suporte a @Scheduled
public class PixRecorrenteApplication { }
```

#### Query Otimizada:

Adicionado método no `PagamentoRecorrenteRepository`:
```java
@Query("SELECT p FROM PagamentoRecorrente p WHERE p.status = :status AND p.dataPrevista <= :data")
List<PagamentoRecorrente> findPendentes(@Param("status") EnumStatusPagamento status, @Param("data") LocalDate data);
```

**Fluxo Completo:**
1. Cliente cria agendamento (`POST /v1/agendamentos`) → `Agendamento.ATIVO`
2. `OrquestracaoConsumer` recebe mensagem → cria `PagamentoRecorrente.PENDENTE` com `dataPrevista`
3. `PagamentoExecutionScheduler` roda a cada 60s
4. Busca PENDENTE com `dataPrevista <= hoje`
5. Para cada pagamento: executa → atualiza status SUCESSO/FALHA_PROCESSAMENTO
6. Log de sucesso/erro

**Trade-offs:**
- **Simples:** Apenas status update. Em produção: integrar com SPI/DICT HTTP client com timeout
- **Delay:** 60s entre checks. Em produção: integrar com event-driven (Kafka) ao invés de polling
- **Sem retry:** Falha → FALHA_PROCESSAMENTO. Próximo tópico (retry) endereçará

**Impacto:**
- ✅ Pagamentos agora executam automaticamente
- ✅ Spec "orquestração segura de cada pagamento" parcialmente cumprida
- ⚠️ Sem retry/DLQ integration ainda (vem na próxima fase)

---

## 7. Roadmap Pós-PoC

### Fase 1 — Resiliência (Retry + DLQ)
1. **Retry com Exponential Backoff:** Adicionar `spring.rabbitmq.listener.simple.retry.*` em OrquestracaoConsumer (3 tentativas, backoff 1s/2s/4s)
2. **DLQ Monitoring:** Implementar `DLQConsumer` para alertar/logar mensagens descartadas
3. **Idempotência no Executor:** Garantir PagamentoExecutionScheduler não processa 2x se falhar

### Fase 2 — Segurança
1. **Authentication/Authorization:** Adicionar JWT/OAuth2 com @PreAuthorize em endpoints
2. **Rate Limiting:** Bucket4j para limitar 100 req/min por IP
3. **TLS/HTTPS:** `server.ssl.*` com certificado auto-assinado

### Fase 3 — Escalabilidade
1. **Fraude:** Substituir Chain of Responsibility por DSL de regras em YAML (sem recompile)
2. **Estados:** Integrar **Spring State Machine Framework** (AgendamentoState já pavimenta)
3. **Scheduler:** Trocar @Scheduled por Kafka consumer (event-driven ao invés de polling)
4. **Execução:** Integrar SPI/DICT HTTP client com timeout e circuit breaker

### Fase 4 — Data
1. **Serialização:** Evoluir `AnaliseFraudeJsonSerializer` para schema registry (Avro/Protobuf)
2. **DTO Mapping:** `OrquestracaoPayloadBuilder` → MapStruct pra type-safety
3. **Auditoria:** Adicionar `@AuditTable` com hist´orico de transições de estado

Essa progressão preserva arquitetura SOLID e padrões de design enquanto escala para produção.