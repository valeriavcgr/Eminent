package com.example.Eminent.auditoria.service;
import com.example.Eminent.auditoria.dto.AuditoriaDTO;
import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.repository.AuditoriaRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository repo;

    /**
     * Registra una nueva entrada en el log de auditoría.
     */
    public void registrar(Usuario usuario, Auditoria.Accion accion, Auditoria.TipoAfectado tipo,
                            Long entidadId, String descripcion, String ip) {
        Auditoria a = new Auditoria();
        a.setUsuario(usuario);
        a.setAccion(accion);
        a.setTipoAfectado(tipo);
        a.setEntidadId(entidadId);
        a.setDescripcion(descripcion);
        a.setIp(ip != null ? ip : obtenerIpActual());
        repo.save(a);
    }

    /**
     * Extrae la IP real del cliente de la petición HTTP actual.
     */
    private String obtenerIpActual() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String ipForwardeada = request.getHeader("X-Forwarded-For");
            return (ipForwardeada != null && !ipForwardeada.isBlank()) ? ipForwardeada : request.getRemoteAddr();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Consulta registros de auditoría con filtros opcionales, paginados.
     */
    public Page<AuditoriaDTO> consultar(Long usuarioId, Auditoria.TipoAfectado tipoAfectado,
                                            LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                            Pageable pageable) {

        LocalDateTime desde = (fechaInicio != null) ? fechaInicio : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime hasta = (fechaFin != null) ? fechaFin : LocalDateTime.of(2100, 1, 1, 0, 0);

        Page<Auditoria> resultados = repo.buscarConFiltros(usuarioId, tipoAfectado, desde, hasta, pageable);

        return resultados.map(a -> {
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
        });
    }

}