package com.example.Eminent.participacion.repository;

import com.example.Eminent.participacion.entity.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    Optional<Inscripcion> findByParticipanteIdAndEventoId(Long participanteId, Long eventoId);
    List<Inscripcion> findByEventoIdAndEstado(Long eventoId, Inscripcion.Estado estado);
    long countByEventoIdAndEstado(Long eventoId, Inscripcion.Estado estado);
    List<Inscripcion> findByEventoIdAndEstadoOrderByFechaInscripcionAsc(Long eventoId, Inscripcion.Estado estado);

}
