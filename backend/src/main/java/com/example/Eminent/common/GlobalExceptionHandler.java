package com.example.Eminent.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Handler global de excepciones para la API REST de Eminent.
 * Captura excepciones de autenticación, autorización y argumentos inválidos,
 * devolviendo respuestas JSON estructuradas con código HTTP y mensaje descriptivo.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja credenciales incorrectas durante el login.
     * Retorna 401 Unauthorized con mensaje de correo o contraseña inválidos.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "mensaje", "Correo o contraseña incorrectos",
                "codigo", 401,
                "timestamp", LocalDateTime.now()));
    }

    /**
     * Maneja intentos de acceso de usuarios inactivos.
     * Retorna 401 Unauthorized indicando que el usuario debe contactar al administrador.
     */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "mensaje", "Usuario inactivo, contacte al administrador",
                "codigo", 401,
                "timestamp", LocalDateTime.now()));
    }

    /**
     * Maneja intentos de acceso a recursos sin los permisos necesarios.
     * Retorna 403 Forbidden con mensaje de permisos insuficientes.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "mensaje", "No tienes permisos para esta acción",
                "codigo", 403,
                "timestamp", LocalDateTime.now()));
    }

    /**
     * Maneja errores de argumentos inválidos (por ejemplo, validación fallida).
     * Retorna 400 Bad Request con el mensaje de error específico.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "mensaje", ex.getMessage(),
                "codigo", 400,
                "timestamp", LocalDateTime.now()));
    }
}