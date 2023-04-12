package com.example.brokergateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private static final String TRUSTED_IPS = "" +
            "hasIpAddress('34.212.75.30') or " +
            "hasIpAddress('34.212.75.30') or " +
            "hasIpAddress('54.218.53.128') or " +
            "hasIpAddress('52.32.178.7')";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http
                .authorizeRequests()
                .anyRequest().access(TRUSTED_IPS);

        return http.build();
    }
}
