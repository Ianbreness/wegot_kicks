package WGK;

import WGK.service.MarcaService;
import java.util.List;
import WGK.domain.Marca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Hace que la lista de marcas activas esté disponible en TODAS las vistas
 * automáticamente, sin tener que agregarla manualmente en cada controller.
 *
 * Esto permite que el sub-nav del fragmentos.html sea dinámico:
 * muestra exactamente las marcas que existen en la BD.
 */
@ControllerAdvice
public class Globalmodeladvice {

    @Autowired
    private MarcaService marcaService;

    @ModelAttribute("marcasNav")
    public List<Marca> marcasNav() {
        return marcaService.getMarcas(true); // solo marcas activas
    }
}