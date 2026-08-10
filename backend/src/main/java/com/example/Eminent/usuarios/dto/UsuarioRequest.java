package com.example.Eminent.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO de entrada para crear/editar un usuario. Recibe los roles como nombres de texto
 * (ADMIN, OPERADOR, MONITOR) en vez de la colección de entidades {@code Rol}.
 *
 * Se reutiliza tanto para crear (todo obligatorio) como para editar (actualización
 * parcial, los campos en null se ignoran) — por eso las anotaciones aquí son solo de
 * formato/rango, nunca de presencia (@NotBlank/@NotNull): con null pasan de largo y no
 * rompen la edición parcial. La obligatoriedad al crear, la unicidad del correo y el
 * dominio @eminent.com se siguen validando en UsuarioService, porque dependen de la
 * operación (crear vs. editar) y de la base de datos.
 */
@Getter
@Setter
public class UsuarioRequest {

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String apellido;

    @Email(message = "El correo electrónico no tiene un formato válido")
    private String correo;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una minúscula y un dígito")
    private String contrasena;

    // "^$|..." además del formato válido acepta cadena vacía: el teléfono es opcional y se
    // puede dejar en blanco intencionalmente al editar (no solo omitirlo).
    @Pattern(regexp = "^$|^\\+?[0-9]{7,15}$", message = "Formato de teléfono inválido. Use +573001234567 o 3001234567")
    private String telefono;

    private List<String> roles;
}
