package com.pix.recorrente.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Metadados da documentação OpenAPI servida em /v3/api-docs e /swagger-ui.html.
 * O conteúdo espelha docs/openapi/openapi.yaml, que permanece como contrato de referência.
 */
@Configuration
public class OpenApiConfig {

    private static final String DESCRICAO = """
            PoC de API para **Pix recorrente** (assinaturas e mensalidades automatizadas).

            ## Fluxo

            1. O cliente registra um agendamento em `POST /v1/agendamentos`.
            2. O **módulo antifraude** avalia a solicitação de forma síncrona e determina o status de risco.
            3. Se aprovado, o agendamento vira `ATIVO` e é publicado no broker.
            4. O consumidor cria a **parcela 1**; cada liquidação bem-sucedida encadeia a parcela seguinte.
            5. Ao atingir `quantidadeParcelas`, o agendamento vira `CONCLUIDO`.

            ## Idempotência

            `POST /v1/agendamentos` exige o header `X-Idempotency-Key`. Uma segunda requisição
            com a mesma chave devolve `409` em vez de criar um agendamento duplicado — a chave
            tem constraint de unicidade no banco.

            ## Códigos de status

            O status HTTP do `POST` reflete a decisão do antifraude:

            | Status | Decisão | Efeito |
            |---|---|---|
            | `201` | `APROVADO` | Agendamento `ATIVO`, parcelas serão geradas |
            | `202` | `REVISAO_MANUAL` | Agendamento `EM_ANALISE`, **nenhuma parcela é gerada** |
            | `422` | `REJEITADO` | Agendamento `REJEITADO_FRAUDE`, nada é publicado no broker |

            Nos casos `202` e `422` a análise não chega ao broker: a fraude barra a operação
            antes da orquestração, então nenhuma cobrança é materializada.
            """;

    static {
        SpringDocUtils.getConfig().replaceWithSchema(LocalDateTime.class, new StringSchema()
                .pattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?$")
                .description("Data-hora sem deslocamento de fuso (java.time.LocalDateTime), no fuso do servidor.")
                .example("2026-07-26T14:00:48.640012"));
    }

    @Bean
    public OpenAPI pixRecorrenteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Pix Recorrente Seguro")
                        .version("1.0.0")
                        .summary("Agendamento de Pix recorrente com análise antifraude e execução assíncrona de parcelas.")
                        .description(DESCRICAO)
                        .contact(new Contact().name("Squad Pix"))
                        .license(new License().name("Proprietary").identifier("LicenseRef-Proprietary")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Execução local (mvn spring-boot:run)"),
                        new Server().url("http://localhost:8081").description("Execução via Docker Compose")));
    }
}
