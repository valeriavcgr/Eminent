package com.example.Eminent.eventos.repository;

import com.example.Eminent.eventos.entity.Evento;
import com.example.Eminent.eventos.entity.Evento.Estado;
import com.example.Eminent.eventos.entity.Evento.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Optional<Evento> findByCreadoPor_Id(Long creadoPorId);

    List<Evento> findByEstado(Estado estado);

    List<Evento> findByEstadoIn(List<Estado> estados);

    boolean existsByNombreAndTipo(String nombre, Tipo tipo);

    List<Evento> findByEstadoAndFechaInicioLessThanEqual(Estado estado, LocalDateTime fecha);

    List<Evento> findByEstadoAndFechaFinLessThanEqual(Estado estado, LocalDateTime fecha);
}