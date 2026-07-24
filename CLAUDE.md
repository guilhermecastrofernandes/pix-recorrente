# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build e Testes
- Compilar: `./mvnw clean compile`
- Rodar testes: `./mvnw test`
- Rodar aplicação: `./mvnw spring-boot:run`

## Diretrizes de Agente
- Sempre consulte as especificações em `specs/` antes de alterar código.
- Siga os padrões arquiteturais descritos em `specs/02-architecture.md`.
- Ao finalizar um componente, execute os testes unitários correspondentes.
