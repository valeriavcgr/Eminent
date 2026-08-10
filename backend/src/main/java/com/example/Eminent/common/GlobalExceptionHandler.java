package com.example.Eminent.common;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * Maneja búsquedas de entidades inexistentes (por ejemplo, un evento o usuario con un id
     * que ya no existe). Retorna 404 Not Found en vez de dejar que se propague como 500.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "mensaje", ex.getMessage(),
                "codigo", 404,
                "timestamp", LocalDateTime.now()));
    }

    /**
     * Maneja fallos de Bean Validation (@Valid) en el cuerpo de la petición.
     * Retorna 400 Bad Request juntando los mensajes de cada campo inválido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of(
                "mensaje", mensaje,
                "codigo", 400,
                "timestamp", LocalDateTime.now()));
    }

    /**
     * Maneja violaciones de integridad referencial (por ejemplo, intentar eliminar un usuario
     * que sigue siendo referenciado por eventos que creó o asistencias que registró).
     * Retorna 409 Conflict con un mensaje claro en vez de dejar que la excepción de base de
     * datos se propague sin manejar.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "mensaje", "No se puede completar la operación porque el registro tiene datos asociados (eventos, asistencias u otros) que dependen de él",
                "codigo", 409,
                "timestamp", LocalDateTime.now()));
    }
}