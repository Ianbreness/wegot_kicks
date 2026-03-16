package WGK.repository;

import WGK.domain.Marca;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    List<Marca> findByActivoTrue();
}