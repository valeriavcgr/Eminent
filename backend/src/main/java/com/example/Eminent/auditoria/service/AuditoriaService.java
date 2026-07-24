package com.example.Eminent.auditoria.service;

import com.example.Eminent.auditoria.entity.Auditoria;
import com.example.Eminent.auditoria.repository.AuditoriaRepository;
import com.example.Eminent.usuarios.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}