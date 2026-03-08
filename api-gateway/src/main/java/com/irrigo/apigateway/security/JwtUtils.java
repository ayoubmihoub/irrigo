package com.irrigo.apigateway.security;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    private String jwtSecret = "votre_cle_tres_secrete_pour_irrigo_ezzayra_solutions_2026_mihoub_ayoub";

    public void validateJwtToken(String authToken) {
        // Cette méthode lèvera une exception si le token est invalide ou expiré
        Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(authToken);
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(jwtSecret).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
}