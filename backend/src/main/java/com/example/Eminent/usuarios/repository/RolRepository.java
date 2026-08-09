package com.example.Eminent.usuarios.repository;

import com.example.Eminent.usuarios.entity.Rol;
import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    /** Busca la entidad Rol del catálogo por su nombre. */
    Optional<Rol> findByNombre(Usuario.Rol nombre);
}
