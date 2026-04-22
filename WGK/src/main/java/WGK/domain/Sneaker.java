package WGK.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
@Entity
@Table(name = "sneaker")
public class Sneaker implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sneaker")
    private Integer idSneaker;

    @Column(nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    private String descripcion;

    @Column(length = 500)
    private String detalle;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal precio;

    private Integer existencias;

    @Column(length = 200)
    private String tallas;

    /** Imagen principal del sneaker */
    @Column(name = "ruta_imagen", length = 1024)
    private String rutaImagen;

    /**
     * Imágenes adicionales separadas por coma.
     * Ejemplo: "/uploads/foto2.jpg,/uploads/foto3.jpg,/uploads/foto4.jpg"
     * Se muestran como thumbnails en la página de detalle.
     */
    @Column(name = "imagenes", length = 2048)
    private String imagenes;

    private boolean activo;

    @ManyToOne
    @JoinColumn(name = "id_marca")
    private Marca marca;
}