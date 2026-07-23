# Makefile cross-platform (macOS, Linux, Windows)
# Detecta SO automaticamente

OS := $(shell uname -s)
ifeq ($(OS),Windows_NT)
	RM = del /Q
	RMDIR = rmdir /S /Q
	MKDIR = mkdir
else
	RM = rm -f
	RMDIR = rm -rf
	MKDIR = mkdir -p
endif

.PHONY: help build compile test run clean package install-deps lint format h2-console version docker-up docker-down docker-build docker-logs docker-restart list-targets setup-completion

# Default target
.DEFAULT_GOAL := help

help:
	@echo "pix-recorrente-api"
	@echo ""
	@echo "Build:"
	@echo "  make build           Compilar + testes"
	@echo "  make compile         Compilar sem testes"
	@echo "  make test            Rodar testes"
	@echo "  make run             Rodar aplicacao"
	@echo "  make clean           Limpar build"
	@echo "  make package         Build JAR"
	@echo ""
	@echo "Dependencies:"
	@echo "  make install-deps    Instalar dependencias"
	@echo "  make lint            Verificar codigo"
	@echo "  make format          Formatar codigo"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-build    Build containers"
	@echo "  make docker-up       Subir containers (-d)"
	@echo "  make docker-down     Parar containers"
	@echo "  make docker-logs     Ver logs"
	@echo "  make docker-restart  Reiniciar containers"
	@echo ""
	@echo "Utils:"
	@echo "  make h2-console      Info H2 Console"
	@echo "  make version         Mostrar versao Maven"
	@echo ""

# Build
build: compile test
	@echo "Build completo OK"

compile:
	mvn clean compile

test:
	mvn test

run:
	mvn spring-boot:run

package:
	mvn clean package -DskipTests

clean:
	mvn clean
	-$(RMDIR) target

install-deps:
	mvn dependency:resolve

# Code quality
lint:
	mvn checkstyle:check || true

format:
	mvn spotless:apply || true

# Docker
docker-build:
	docker compose build

docker-up:
	docker compose up -d
	@echo "Containers iniciados"

docker-down:
	docker compose down
	@echo "Containers parados"

docker-restart: docker-down docker-up
	@echo "Containers reiniciados"

docker-logs:
	docker compose logs -f

docker-status:
	docker compose ps

# Utilities
h2-console:
	@echo "H2 Console disponivel em: http://localhost:8080/h2-console"
	@echo "JDBC URL: jdbc:h2:mem:testdb"
	@echo "Username: sa"
	@echo "Password: (vazio)"

version:
	mvn --version
	@echo ""
	@java -version 2>&1 || echo "Java nao instalado"

# Completion
list-targets:
	@echo "Targets disponiveis:"
	@make -qp 2>/dev/null | grep -E "^[a-zA-Z_][a-zA-Z0-9_-]*:" | cut -d: -f1 | sort -u | sed 's/^/  /'

setup-completion:
	@echo "Configurando shell completion..."
	@bash setup-completion.sh
