package com.github.jorgebsa.spring.demo.ws;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SecurityScheme(
        name = OpenAPIConfig.SCHEME_NAME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "spring-demo",
                version = "v1"
        )
)
class OpenAPIConfig {

    public static final String SCHEME_NAME = "access_token";

    private OpenAPIConfig() {

    }

}
