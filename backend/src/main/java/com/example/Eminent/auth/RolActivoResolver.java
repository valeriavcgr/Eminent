package com.example.Eminent.auth;

import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.stereotype.Component;

/**
 * Resuelve el "rol activo" con el que un usuario está operando en la sesión actual, a partir
 * del header opcional {@code X-Rol-Activo} enviado por el frontend. Es solo una preferencia de
 * presentación (qué subconjunto de datos devolver en dashboard/asistencia); la autorización real
 * sigue siendo la de {@code @PreAuthorize} basada en los roles reales del usuario en base de datos.
 */
@Component
public class RolActivoResolver {

    public static final String HEADER = "X-Rol-Activo";

    /**
     * Devuelve el rol activo efectivo: el del header si el usuario realmente lo tiene,
     * o el de mayor rango entre sus roles como fallback silencioso en cualquier otro caso.
     */
    public Usuario.Rol resolver(Usuario usuario, String headerRolActivo) {
        if (headerRolActivo != null) {
            try {
                Usuario.Rol candidato = Usuario.Rol.valueOf(headerRolActivo.toUpperCase());
                if (usuario.tieneRol(candidato)) {
                    return candidato;
                }
            } catch (IllegalArgumentException ignored) {
                // valor no reconocido en el header -> cae al fallback
            }
        }
        return usuario.rolDeMayorRango();
    }
}
