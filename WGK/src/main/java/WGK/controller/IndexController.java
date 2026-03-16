package WGK.controller;

import WGK.service.MarcaService;
import WGK.service.SneakerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
        model.addAttribute("sneakers", sneakerService.getSneakers(true));
        model.addAttribute("marcas", marcaService.getMarcas(true));
        return "/index";
    }

    @GetMapping("/marca/{idMarca}")
    public String porMarca(@PathVariable Integer idMarca, Model model) {
        model.addAttribute("sneakers", sneakerService.getPorMarca(idMarca));
        model.addAttribute("marcas", marcaService.getMarcas(true));
        model.addAttribute("idMarcaActual", idMarca);
        return "/index";
    }
}