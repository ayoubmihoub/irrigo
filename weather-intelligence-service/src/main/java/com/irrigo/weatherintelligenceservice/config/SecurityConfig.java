package com.irrigo.weatherintelligenceservice.config;

import com.irrigo.weatherintelligenceservice.security.AuthTokenFilter;
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

    // On déclare le filtre comme un Bean pour qu'il puisse utiliser @Autowired (JwtUtils)
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Désactivation du CSRF car nous utilisons des tokens JWT (stateless)
                .csrf(csrf -> csrf.disable())

                // 2. Gestion de session stateless (pas de cookies/sessions côté serveur)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Configuration des règles d'accès
                .authorizeHttpRequests(auth -> auth
                        // On peut laisser l'accès libre à Swagger ou aux endpoints de santé si nécessaire
                        // .requestMatchers("/api/weather/health").permitAll()

                        // Toutes les autres requêtes doivent être authentifiées
                        .anyRequest().authenticated()
                );

        // 4. Ajout de votre filtre JWT avant le filtre d'authentification standard de Spring
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}