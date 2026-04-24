package com.irrigo.farmservice.config;

import com.irrigo.farmservice.security.AuthTokenFilter;
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
                        // --- EXCEPTIONS POUR L'ESP32 ET L'IA ---
                        // On autorise l'accès sans token uniquement pour ces routes spécifiques
                        .requestMatchers("/api/farms/*/moisture").permitAll()
                        .requestMatchers("/api/farms/*/irrigation-status").permitAll()
                        .requestMatchers("/api/farms/*/moisture-value").permitAll()

                        // --- SÉCURITÉ POUR LE RESTE ---
                        // Toutes les autres actions (créer, supprimer, lister les champs) exigent un Token
                        .requestMatchers("/api/farms/**").authenticated()
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}