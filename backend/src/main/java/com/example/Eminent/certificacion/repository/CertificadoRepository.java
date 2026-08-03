package com.example.Eminent.certificacion.repository;

import com.example.Eminent.certificacion.entity.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {
    Optional<Certificado> findByAsistenciaId(Long asistenciaId);
    Optional<Certificado> findByCodigoUnico(String codigoUnico);
}