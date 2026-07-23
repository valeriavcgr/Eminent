@Entity
@Table(name="evento")
public class Evento{

    private Long evento_id;
    private String nombre;
    public enum Tipo{
        Taller,
        Capacitacion,
        Torneo
    }
    private Tipo tipo;
    public enum Modalidad{
        Presencial,
        Virtual
    }
    private Modalidad modalidad;
    private String descripcion;
    private LocalDatefechaInicio;
    private LocalDate fechaFin;
    private Integer Aforo;

    public enum Estado{
        Programado,
        EnCurso,
        Finalizado,
        Cancelado
    }
    private Estado estado;
    private Long creadoPor;
    private LocalDateTime fechaCreacion;

}