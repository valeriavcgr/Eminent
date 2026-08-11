package com.example.Eminent.usuarios.repository;

import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca un usuario por su correo electrónico. */
    Optional<Usuario> findByCorreo(String correo);

    /** Verifica si existe un usuario con el correo dado. */
    boolean existsByCorreo(String correo);

    /** Lista paginada de usuarios que tengan el rol dado entre sus roles asignados. */
    @Query("SELECT u FROM Usuario u JOIN u.roles r WHERE r.nombre = :nombre")
    Page<Usuario> findByRolesNombre(@Param("nombre") Usuario.Rol nombre, Pageable pageable);
}