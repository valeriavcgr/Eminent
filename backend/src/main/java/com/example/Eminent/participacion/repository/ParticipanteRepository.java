package com.example.Eminent.participacion.repository;

import com.example.Eminent.participacion.entity.Participante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Participante}.
 * Proporciona operaciones CRUD estándar más consultas personalizadas
 * para búsquedas por documento, correo y nombre/apellido.
 */
public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    /** Busca un participante por su número de documento. */
    Optional<Participante> findByDocumento(String documento);

    /** Verifica si existe un participante con el documento dado. */
    boolean existsByDocumento(String documento);

    /** Busca un participante por su correo electrónico. */
    Optional<Participante> findByCorreo(String correo);

    /** Busca participantes cuyo nombre o apellido contenga los términos dados (búsqueda insensible a mayúsculas). */
    List<Participante> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}