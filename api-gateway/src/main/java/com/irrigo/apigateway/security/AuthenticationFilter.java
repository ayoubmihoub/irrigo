package com.irrigo.apigateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private JwtUtils jwtUtils;

    public AuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (ServerWebExchange exchange, GatewayFilterChain chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // --- ADAPTATION POUR L'ESP32 ET L'IA ---
            // On laisse passer les routes publiques ET les endpoints IoT sans vérifier le token
            if (path.contains("/api/users/login") ||
                    path.contains("/api/users/register") ||
                    path.contains("/moisture") ||
                    path.contains("/irrigation-status")) {
                return chain.filter(exchange);
            }

            // --- LOGIQUE DE VÉRIFICATION DU TOKEN (Pour Web/Mobile) ---
            if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Header Authorization manquant", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // Si le token est invalide, on bloque
                if (!jwtUtils.validateJwtToken(token)) {
                    return onError(exchange, "Token JWT invalide", HttpStatus.UNAUTHORIZED);
                }
            } else {
                return onError(exchange, "Format du header Authorization invalide", HttpStatus.UNAUTHORIZED);
            }

            // Si tout est ok (Token valide), on continue la chaîne vers le microservice
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }
}