package WGK.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
@Entity
@Table(name = "marca")
public class Marca implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String descripcion;

    @Column(name = "ruta_imagen", length = 1024)
    private String rutaImagen;

    private boolean activo;

    @OneToMany(mappedBy = "marca", fetch = FetchType.LAZY)
    private List<Sneaker> sneakers;
}