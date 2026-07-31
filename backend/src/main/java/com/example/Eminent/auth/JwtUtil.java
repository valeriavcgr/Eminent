package com.example.Eminent.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utilidad para la generación, validación y extracción de información
 * desde tokens JWT utilizados en la autenticación de la API REST.
 * Utiliza una clave HMAC-SHA256 fija y tokens con expiración de 1 hora.
 */
@Component
public class JwtUtil {

    /** Clave secreta HMAC-SHA256 para firmar y verificar tokens JWT. */
    private final Key key = Keys.hmacShaKeyFor(
            "clave-secreta-proyecto-eminent-caro".getBytes());
    /** Tiempo de expiración del token en milisegundos (1 hora). */
    private final long EXPIRATION_MS = 3600000;

    /**
     * Genera un token JWT para el correo y rol del usuario proporcionados.
     * El token incluye el subject (correo), el rol como claim y la fecha de expiración.
     */
    public String generarToken(String correo, String rol) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valida el token JWT y extrae todos los claims contenidos en él.
     * Lanza excepción si el token es inválido o ha expirado.
     */
    public Claims validarYExtraer(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    /** Extrae el correo (subject) del token JWT. */
    public String extraerCorreo(String token) {
        return validarYExtraer(token).getSubject();
    }

    /** Extrae el rol del usuario almacenado en el claim "rol" del token JWT. */
    public String extraerRol(String token) {
        return validarYExtraer(token).get("rol", String.class);
    }

    /** Verifica si el token JWT es válido (no expirado, firma correcta). */
    public boolean esValido(String token) {
        try {
            validarYExtraer(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}