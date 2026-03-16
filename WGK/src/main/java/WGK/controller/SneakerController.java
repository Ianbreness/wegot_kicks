package WGK.controller;

import WGK.domain.Sneaker;
import WGK.service.MarcaService;
import WGK.service.SneakerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/sneaker")
public class SneakerController {

    @Autowired
    private SneakerService sneakerService;

    @Autowired
    private MarcaService marcaService;

    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("sneakers", sneakerService.getSneakers(false));
        model.addAttribute("totalSneakers", sneakerService.getSneakers(false).size());
        model.addAttribute("sneaker", new Sneaker());
        model.addAttribute("marcas", marcaService.getMarcas(true));
        return "/sneaker/listado";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable Integer id, Model model) {
        Sneaker sneaker = sneakerService.getSneaker(id);
        if (sneaker == null) sneaker = new Sneaker();
        model.addAttribute("sneaker", sneaker);
        model.addAttribute("marcas", marcaService.getMarcas(true));
        return "/sneaker/modifica";
    }

    @PostMapping("/guardar")
    public String guardar(Sneaker sneaker,
            @RequestParam("imagenFile") MultipartFile imagenFile) throws Exception {
        sneakerService.save(sneaker, imagenFile);
        return "redirect:/sneaker/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("idSneaker") Integer idSneaker) throws Exception {
        sneakerService.delete(idSneaker);
        return "redirect:/sneaker/listado";
    }
}