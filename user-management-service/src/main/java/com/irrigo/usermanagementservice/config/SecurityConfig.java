package com.irrigo.usermanagementservice.config;

import com.irrigo.usermanagementservice.security.AuthTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Déclaration du filtre JWT (celui que tu vas créer dans le package security)
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    // 2. Encodeur pour les mots de passe (utilisé par DataInitializer et le Login)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 3. Gestionnaire d'authentification (nécessaire pour le LoginController)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 4. Configuration principale de la chaîne de filtres
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactivation du CSRF car nous utilisons des tokens (JWT)
                .csrf(csrf -> csrf.disable())

                // Gestion de session STATELESS (pas de cookies, juste le token)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configuration des accès aux routes
                .authorizeHttpRequests(auth -> auth
                        // Routes publiques : login et register sont ouverts à tous
                        .requestMatchers("/api/users/login/**", "/api/users/register/**").permitAll()

                        // Routes protégées : par exemple /api/users/all demande d'être authentifié
                        .anyRequest().authenticated()
                );

        // AJOUT CRUCIAL : On injecte notre filtre JWT AVANT le filtre de login standard
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}