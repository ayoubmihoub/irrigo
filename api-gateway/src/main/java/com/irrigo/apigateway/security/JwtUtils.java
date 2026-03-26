package com.irrigo.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtils {
    // La clé doit être strictement la même
    private String jwtSecret = "votre_cle_tres_secrete_pour_irrigo_ezzayra_solutions_2026_mihoub_ayoub";

    // AJOUT : La méthode pour générer la clé de la même manière que le User Service
    private Key getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public void validateJwtToken(String authToken) {
        // CORRECTION : Utilisation de getSigningKey() au lieu de la String brute
        Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
    }

    public String getEmailFromToken(String token) {
        // CORRECTION : Utilisation de getSigningKey() ici aussi
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}