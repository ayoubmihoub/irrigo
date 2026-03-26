package com.irrigo.taskmanagementservice.config;

import com.irrigo.taskmanagementservice.security.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. ENDPOINTS PUBLICS (ACCESSIBLES PAR LES AUTRES MICROSERVICES)
                        .requestMatchers("/api/tasks/crops/unique").permitAll()
                        .requestMatchers("/api/tasks/history/**").permitAll() // AUTORISATION DE L'HISTORIQUE

                        // 2. TOUT LE RESTE RESTE SÉCURISÉ (JWT REQUIS)
                        .requestMatchers("/api/tasks/**").authenticated()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}