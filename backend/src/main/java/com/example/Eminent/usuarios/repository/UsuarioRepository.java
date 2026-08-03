package com.example.Eminent.usuarios.repository;

import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca un usuario por su correo electrónico. */
    Optional<Usuario> findByCorreo(String correo);

    /** Verifica si existe un usuario con el correo dado. */
    boolean existsByCorreo(String correo);

    /** Lista paginada de usuarios filtrados por su rol. */
    Page<Usuario> findByRol(Usuario.Rol rol, Pageable pageable);
}