package com.mx.cbtis.gestor.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MonitoreoSecurityConfig {

    @Bean
    public SecurityFilterChain seguridadActuator(HttpSecurity http)
            throws Exception {

        http
            .requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeRequests(reglas -> reglas
                .requestMatchers(EndpointRequest.to("health")).permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}