package com.example.Eminent.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

// crea el token al iniciar sesion y lo lee para recordar la sesión

@Component
public class JwtUtil {

    private final Key key;
    // Tiempo de expiración del token en milisegundos (1 hora)
    private final long EXPIRATION_MS = 3600000;

    public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // devuelve el token
    public String generarToken(String correo, List<String> roles) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // lee el token y lo valida con el creado
    public Claims validarYExtraer(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }

    // Extrae el correo (subject) del token JWT
    public String extraerCorreo(String token) {
        return validarYExtraer(token).getSubject();
    }

    // Extrae la lista de roles del usuario
    @SuppressWarnings("unchecked")
    public List<String> extraerRoles(String token) {
        return validarYExtraer(token).get("roles", List.class);
    }

    // Verifica si el token JWT es válido (no expirado, firma correcta)
    public boolean esValido(String token) {
        try {
            validarYExtraer(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}