package WGK.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String nombre;

    @Column(nullable = false, length = 100, unique = true)
    @NotNull
    @Email
    private String correo;

    @Column(nullable = false, length = 255)
    @NotNull
    private String password;

    @Column(length = 20)
    private String telefono;

    private boolean activo;
}