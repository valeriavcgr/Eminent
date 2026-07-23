@Entity
@Table(name="asistencia")
public class Asistencia{
    private Long id;
    private LocalDate fechaHora;
    public enum Metodo{
        CHECK,
        manual
    }
    private Metodo metodo;
    private Long registradoPor;

}