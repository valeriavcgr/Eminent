package com.example.Eminent.auditoria.service;

import com.example.Eminent.auditoria.dto.AuditoriaDTO;
import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.repository.AuditoriaRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository repo;

    public void registrar(Usuario usuario, Auditoria.Accion accion, Auditoria.TipoAfectado tipo,
                          Long entidadId, String descripcion, String ip) {
        Auditoria a = new Auditoria();
        a.setUsuario(usuario);
        a.setAccion(accion);
        a.setTipoAfectado(tipo);
        a.setEntidadId(entidadId);
        a.setDescripcion(descripcion);
        a.setIp(ip);
        repo.save(a);
    }

    public List<AuditoriaDTO> consultar(Long usuarioId, Auditoria.TipoAfectado tipoAfectado,
                                        LocalDateTime fechaInicio, LocalDateTime fechaFin) {

        LocalDateTime desde = (fechaInicio != null) ? fechaInicio : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime hasta = (fechaFin != null) ? fechaFin : LocalDateTime.of(2100, 1, 1, 0, 0);

        List<Auditoria> resultados = repo.buscarConFiltros(usuarioId, tipoAfectado, desde, hasta);

        return resultados.stream().map(a -> {
            AuditoriaDTO dto = new AuditoriaDTO();
            dto.setId(a.getId());
            dto.setUsuarioId(a.getUsuario() != null ? a.getUsuario().getId() : null);
            dto.setNombreUsuario(a.getUsuario() != null ? a.getUsuario().getNombre() + " " + a.getUsuario().getApellido() : "Sistema");
            dto.setAccion(a.getAccion().name());
            dto.setTipoAfectado(a.getTipoAfectado().name());
            dto.setEntidadId(a.getEntidadId());
            dto.setDescripcion(a.getDescripcion());
            dto.setIp(a.getIp());
            dto.setFechaHora(a.getFechaHora());
            return dto;
        }).toList();
    }


}