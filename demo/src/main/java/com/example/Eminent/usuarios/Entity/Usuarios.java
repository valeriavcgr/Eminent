@Entity
@Table(name="usuarios")
public class Usuarios {

    @Id
    @Column(name= "usuario_id")
    GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long UsuarioId;

    private String nombre;
    private String apellido;
    private email correo;
    private String contraseña;
    private integer telefono;

    public enum Rol{
        Administrador,
        Operador,
        Monitor,
        Invitado
    }

    private Rol rol;
    public enum Estado{
        Activo,
        InActivo
    }

    private Estado estado;
    private LocalDate fechaCreacion;


}