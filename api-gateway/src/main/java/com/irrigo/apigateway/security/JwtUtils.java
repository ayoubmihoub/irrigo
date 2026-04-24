package com.irrigo.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // La clé doit être strictement la même dans tous les microservices
    private final String jwtSecret = "votre_cle_tres_secrete_pour_irrigo_ezzayra_solutions_2026_mihoub_ayoub";

    // Méthode pour générer la clé de signature de manière sécurisée
    private Key getSigningKey() {
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * CORRECTION : La méthode renvoie maintenant un boolean pour être utilisée
     * avec l'opérateur '!' dans votre AuthenticationFilter.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            return true; // Le token est valide
        } catch (Exception e) {
            // Log de l'erreur pour le débogage
            logger.error("Validation JWT échouée : {}", e.getMessage());
            return false; // Le token est invalide ou expiré
        }
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}