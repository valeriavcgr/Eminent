package com.example.Eminent.certificacion.repository;

import com.example.Eminent.certificacion.entity.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link Certificado}.
 * Proporciona operaciones CRUD estándar más consultas para buscar certificados
 * por ID de asistencia o por código único de verificación.
 */
public interface CertificadoRepository extends JpaRepository<Certificado, Long> {

    /** Busca un certificado por el ID de su asistencia vinculada. */
    Optional<Certificado> findByAsistenciaId(Long asistenciaId);

    /** Busca un certificado por su código único de verificación. */
    Optional<Certificado> findByCodigoUnico(String codigoUnico);
}