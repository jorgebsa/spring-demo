package com.github.jorgebsa.spring.demo.security;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class KeycloakConfiguration {

    /*
     * Since Spring Boot 2.6.0, circular references are blocked by default.
     * The current implementation of KeycloakWebSecurityConfigurerAdapter
     * introduces a circular reference on field `keycloakConfigResolver`
     * if the class is used to instantiate that bean as well. This is why
     * the bean initialization has been moved to this external class.
     */
    @Bean
    KeycloakSpringBootConfigResolver keycloakSpringBootConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }
}
