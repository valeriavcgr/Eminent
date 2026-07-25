package com.example.Eminent.eventos.repository;

import com.example.Eminent.eventos.entity.EventoMonitor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventoMonitorRepository extends JpaRepository<EventoMonitor, Long> {

    List<EventoMonitor> findByMonitor_Id(Long monitorId);

    List<EventoMonitor> findByEvento_Id(Long eventoId);

    boolean existsByEvento_IdAndMonitor_Id(Long eventoId, Long monitorId);
}