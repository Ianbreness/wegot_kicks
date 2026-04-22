package WGK.controller;

import WGK.service.MarcaService;
import WGK.service.SneakerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class IndexController {

    private final SneakerService sneakerService;
    private final MarcaService marcaService;

    public IndexController(SneakerService sneakerService, MarcaService marcaService) {
        this.sneakerService = sneakerService;
        this.marcaService = marcaService;
    }

    @GetMapping("/")
    public String index(Model model) {
        List sneakers = sneakerService.getSneakers(true);
        model.addAttribute("sneakers", sneakers);
        model.addAttribute("marcas", marcaService.getMarcas(true));
        // Primeros 4 para el carrusel
        int limite = Math.min(4, sneakers.size());
        model.addAttribute("carruselSneakers", sneakers.subList(0, limite));
        return "/index";
    }

    @GetMapping("/marca/{idMarca}")
    public String porMarca(@PathVariable Integer idMarca, Model model) {
        List sneakers = sneakerService.getSneakers(true);
        model.addAttribute("sneakers", sneakerService.getPorMarca(idMarca));
        model.addAttribute("marcas", marcaService.getMarcas(true));
        model.addAttribute("idMarcaActual", idMarca);
        int limite = Math.min(4, sneakers.size());
        model.addAttribute("carruselSneakers", sneakers.subList(0, limite));
        return "/index";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam("q") String q, Model model) {
        List sneakers = sneakerService.getSneakers(true);
        model.addAttribute("sneakers", sneakerService.buscarPorNombre(q));
        model.addAttribute("marcas", marcaService.getMarcas(true));
        model.addAttribute("busqueda", q);
        int limite = Math.min(4, sneakers.size());
        model.addAttribute("carruselSneakers", sneakers.subList(0, limite));
        return "/index";
    }
}
