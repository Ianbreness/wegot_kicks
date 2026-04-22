package WGK.controller;

import WGK.domain.Marca;
import WGK.service.MarcaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/marca")
public class MarcaController {

    @Autowired
    private MarcaService marcaService;

    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("marcas", marcaService.getMarcas(false));
        model.addAttribute("totalMarcas", marcaService.getMarcas(false).size());
        model.addAttribute("marca", new Marca());
        return "/marca/listado";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable Integer id, Model model) {
        Marca marca = marcaService.getMarca(id).orElse(new Marca());
        model.addAttribute("marca", marca);
        return "/marca/modifica";
    }

    @PostMapping("/guardar")
    public String guardar(Marca marca) throws Exception {
        marcaService.save(marca);
        return "redirect:/marca/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("idMarca") Integer idMarca) throws Exception {
        marcaService.delete(idMarca);
        return "redirect:/marca/listado";
    }
}