package io.teletronics.storage_app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi customOpenApi() {
        return GroupedOpenApi.builder()
                .group("api")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI swaggerUiConfigParameters() {
        return new OpenAPI()
                .info(new Info()
                        .title("Teletronics Storage Application")
                        .version("1.0.0")
                );
    }
}
