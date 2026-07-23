package com.example.Eminent.usuarios;

@Entity
@Table(name="Auditoria")
public class Auditoria{
    private Long id;
    public enum Accion{
        ver,
        crear,
        editar,
        cancelar
    }
    private Accion accion;
    public enum Tipo{
        evento,
        participacion,
        inscripcion,
        asistencia,
        certificado
    }
    private Tipo tipo;
    private String descripcin;
    private LocalDate fechaHora;
}