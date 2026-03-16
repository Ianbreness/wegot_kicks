package WGK.repository;

import WGK.domain.Sneaker;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SneakerRepository extends JpaRepository<Sneaker, Integer> {

    List<Sneaker> findByActivoTrue();

    List<Sneaker> findByMarcaIdMarca(Integer idMarca);

    List<Sneaker> findByPrecioBetweenOrderByPrecioAsc(BigDecimal precioInf, BigDecimal precioSup);

    @Query("SELECT s FROM Sneaker s WHERE s.precio BETWEEN :precioInf AND :precioSup ORDER BY s.precio ASC")
    List<Sneaker> buscarPorRangoPrecioJPQL(
            @Param("precioInf") BigDecimal precioInf,
            @Param("precioSup") BigDecimal precioSup);

    @Query(value = "SELECT * FROM sneaker WHERE precio BETWEEN :precioInf AND :precioSup ORDER BY precio ASC",
           nativeQuery = true)
    List<Sneaker> buscarPorRangoPrecioSQL(
            @Param("precioInf") BigDecimal precioInf,
            @Param("precioSup") BigDecimal precioSup);

    List<Sneaker> findByDescripcionContainingIgnoreCase(String descripcion);
}