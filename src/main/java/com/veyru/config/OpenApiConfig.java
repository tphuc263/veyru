package com.veyru.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openApi(
      @Value("${open.api.title}") String title,
      @Value("${open.api.version}") String version,
      @Value("${open.api.description}") String description,
      @Value("${open.api.serverUrl}") String serverUrl) {

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
                .title(title)
                .version(version)
                .description(description)
                .license(new License().name("Proprietary")))
        .servers(List.of(new Server().url(serverUrl).description("Veyru API server")));
  }
}
