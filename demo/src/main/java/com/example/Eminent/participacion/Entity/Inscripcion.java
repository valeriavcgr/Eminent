@Entity
Table(name="inscripcion")
    public class Inscripcion{

    private Long inscripciones_id;
    private LocalDate fechaInscripcion;

    public enum MetodoInscricion{
        Formulario,
        CSV
    }
    private MetodoInscripcion;
    private Long codigoQR;

    public enum Estado{
        Activa,
        EnEspera
    }
    private Estado estado;

    }