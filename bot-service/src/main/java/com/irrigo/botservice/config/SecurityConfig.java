package com.irrigo.botservice.config;

import com.irrigo.botservice.security.AuthTokenFilter;
import com.irrigo.botservice.security.JwtUtils; // Assurez-vous d'importer JwtUtils
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

    // On définit le bean avec le paramètre JwtUtils
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(JwtUtils jwtUtils) {
        return new AuthTokenFilter(jwtUtils);
    }

    // On injecte JwtUtils ici pour pouvoir l'utiliser dans l'appel ci-dessous
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtils jwtUtils) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/bot/**").authenticated()
                                .anyRequest().authenticated()
                );

        // CORRECTION : On passe l'argument jwtUtils requis par la méthode
        http.addFilterBefore(authenticationJwtTokenFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}