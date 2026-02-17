package com.irrigo.apigateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain; // Indispensable pour .filter()
import reactor.core.publisher.Mono; // Pour le retour réactif

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
        // On précise (ServerWebExchange exchange, GatewayFilterChain chain)
        return (ServerWebExchange exchange, GatewayFilterChain chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // Routes publiques : on laisse passer sans vérifier le token
            if (path.contains("/api/users/login") || path.contains("/api/users/register")) {
                return chain.filter(exchange); // L'erreur sur 'filter' doit disparaître ici
            }

            // ... reste de ta logique de vérification de token ...

            // Si tout est ok, on continue la chaîne
            return chain.filter(exchange);
        };
    }

    // Méthode d'erreur réactive
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }
}