package com.example.Eminent.participacion.repository;

import com.example.Eminent.participacion.entity.Participante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ParticipanteRepository extends JpaRepository<Participante, Long> {
    Optional<Participante> findByDocumento(String documento);
    boolean existsByDocumento(String documento);
    Optional<Participante> findByCorreo(String correo);
    List<Participante> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}
