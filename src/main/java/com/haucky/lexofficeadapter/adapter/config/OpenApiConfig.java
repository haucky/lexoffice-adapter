package com.haucky.lexofficeadapter.adapter.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Lexoffice Adapter API",
        version = "${api.version}",
        description = "REST API for interfacing with Lexoffice services",
        contact = @Contact(
            name = "API Support",
            email = "support@haucky.com"
        )
    ),
        servers = {
                @Server(url = "/", description = "Default Server URL")
        }
)
@SecurityScheme(
    name = "bearer-jwt",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT token authentication. Use /v1/tokens endpoints to generate tokens for testing."
)
public class OpenApiConfig {
}