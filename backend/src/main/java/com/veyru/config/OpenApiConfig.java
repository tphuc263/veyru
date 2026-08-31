package com.veyru.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApi(OpenApiProperties properties) {

    final String securitySchemeName = "cookieAuth";

    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(
            new Components()
                .addSecuritySchemes(
                    securitySchemeName,
                    new SecurityScheme()
                        .name("veyru_access")
                        .in(SecurityScheme.In.COOKIE)
                        .type(SecurityScheme.Type.APIKEY)))
        .info(
            new Info()
                .title(properties.title())
                .version(properties.version())
                .description(properties.description())
                .license(new License().name("Proprietary")))
        .servers(
            List.of(
                new Server()
                    .url(properties.serverUrl().toString())
                    .description("Veyru API server")));
  }
}
