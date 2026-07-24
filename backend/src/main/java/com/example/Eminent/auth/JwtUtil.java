package com.example.Eminent.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key = Keys.hmacShaKeyFor(
            "clave-secreta-proyecto-eminent-caro".getBytes());
    private final long EXPIRATION_MS = 3600000; // 1 hora

    public String generarToken(String correo, String rol) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validarYExtraer(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    public String extraerCorreo(String token) {
        return validarYExtraer(token).getSubject();
    }

    public String extraerRol(String token) {
        return validarYExtraer(token).get("rol", String.class);
    }

    public boolean esValido(String token) {
        try {
            validarYExtraer(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}