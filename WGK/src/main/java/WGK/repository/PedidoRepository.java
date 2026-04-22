package WGK.repository;

import WGK.domain.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    
    List<Pedido> findByUsuarioOrderByFechaDesc(String usuario);
}