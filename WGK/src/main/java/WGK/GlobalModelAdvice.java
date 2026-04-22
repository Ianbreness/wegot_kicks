package WGK;

import WGK.service.MarcaService;
import java.util.List;
import WGK.domain.Marca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private MarcaService marcaService;

    @ModelAttribute("marcasNav")
    public List<Marca> marcasNav() {
        return marcaService.getMarcas(true); // solo marcas activas
    }
}