@Entity
@Table(name="certificacion")
public class Certificacion{

    private Long id;
    private Long codigoUnico;
    private Integer duracionHoras;
    private LocalDate fechaEmision;
    private String rutaPDF;
    private Long codigoQRVerificacion;

}