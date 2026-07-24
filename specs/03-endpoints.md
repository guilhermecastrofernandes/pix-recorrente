---

### `03-endpoints.md`

```markdown
# 03. Documentação de Endpoints RESTful

Documentação da interface HTTP da API de Pix Recorrente.

---

## 1. Criar Agendamento Recorrente

Registra uma nova assinatura/recorrência Pix e executa a verificação síncrona de fraude.

* **HTTP Method:** `POST`
* **Path:** `/v1/agendamentos`
* **Headers:**
    * `Content-Type: application/json`
    * `X-Idempotency-Key: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d` *(Obrigatório para prevenção de duplicidade)*

### Request Body
```json
{
  "clienteId": "12345678900",
  "chavePixRecebedor": "financeiro@empresa.com.br",
  "valor": 150.00,
  "frequencia": "MENSAL",
  "dataInicio": "2026-08-01",
  "quantidadeParcelas": 12
}
Responses
210 Created — Agendamento Aprovado
JSON
{
  "id": "a4e82b78-012a-4c20-a611-37d4554b2d12",
  "status": "ATIVO",
  "clienteId": "12345678900",
  "chavePixRecebedor": "financeiro@empresa.com.br",
  "valor": 150.00,
  "frequencia": "MENSAL",
  "dataInicio": "2026-08-01",
  "analiseFraude": {
    "statusRisco": "APROVADO",
    "score": 5,
    "regrasVioladas": [],
    "dataAnalise": "2026-07-23T09:25:00Z"
  },
  "dataCriacao": "2026-07-23T09:25:00Z"
}
202 Accepted — Necessita Revisão Manual (Suspeita de Fraude)
JSON
{
  "id": "b5f93c89-123b-5d31-b722-48e5665c3e23",
  "status": "EM_ANALISE",
  "analiseFraude": {
    "statusRisco": "REVISAO_MANUAL",
    "score": 65,
    "regrasVioladas": ["RNF-01: Valor individual superior a R$ 5.000,00"],
    "dataAnalise": "2026-07-23T09:25:00Z"
  }
}
422 Unprocessable Entity — Rejeitado pelo Antifraude
JSON
{
  "codigo": "AGENDAMENTO_REJEITADO_FRAUDE",
  "mensagem": "A solicitação de agendamento foi negada pelas regras de segurança.",
  "analiseFraude": {
    "statusRisco": "REJEITADO",
    "score": 95,
    "regrasVioladas": ["RNF-02: Chave Pix cadastrada em lista de risco nacional"],
    "dataAnalise": "2026-07-23T09:25:00Z"
  }
}
2. Consultar Agendamento por ID
Obtém o estado consolidado do agendamento e o histórico de cobranças associadas.

HTTP Method: GET

Path: /v1/agendamentos/{id}

Responses
200 OK
JSON
{
  "id": "a4e82b78-012a-4c20-a611-37d4554b2d12",
  "status": "ATIVO",
  "clienteId": "12345678900",
  "chavePixRecebedor": "financeiro@empresa.com.br",
  "valor": 150.00,
  "frequencia": "MENSAL",
  "dataInicio": "2026-08-01",
  "analiseFraude": {
    "statusRisco": "APROVADO",
    "score": 5,
    "regrasVioladas": []
  },
  "pagamentos": [
    {
      "id": "f81d4fae-7dec-11d0-a765-00a0c91e6bf6",
      "dataPrevista": "2026-08-01",
      "valor": 150.00,
      "status": "SUCESSO",
      "dataExecucao": "2026-08-01T06:00:12Z"
    }
  ]
}
404 Not Found
JSON
{
  "codigo": "AGENDAMENTO_NAO_ENCONTRADO",
  "mensagem": "Nenhum agendamento localizado para o ID informado."
}