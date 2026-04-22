package WGK.controller;

import WGK.domain.Sneaker;
import WGK.service.MarcaService;
import WGK.service.SneakerService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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

    // ── Listado admin ────────────────────────────────────────────────────
    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("sneakers", sneakerService.getSneakers(false));
        model.addAttribute("totalSneakers", sneakerService.getSneakers(false).size());
        model.addAttribute("sneaker", new Sneaker());
        model.addAttribute("marcas", marcaService.getMarcas(true));
        return "/sneaker/listado";
    }

    // ── Formulario edición ───────────────────────────────────────────────
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable Integer id, Model model) {
        Sneaker sneaker = sneakerService.getSneaker(id);
        if (sneaker == null) sneaker = new Sneaker();
        model.addAttribute("sneaker", sneaker);
        model.addAttribute("marcas", marcaService.getMarcas(true));
        return "/sneaker/modifica";
    }

    // ── Guardar ──────────────────────────────────────────────────────────
    @PostMapping("/guardar")
    public String guardar(Sneaker sneaker,
            @RequestParam("imagenFile") MultipartFile imagenFile) throws Exception {
        sneakerService.save(sneaker, imagenFile);
        return "redirect:/sneaker/listado";
    }

    // ── Eliminar ─────────────────────────────────────────────────────────
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("idSneaker") Integer idSneaker) throws Exception {
        sneakerService.delete(idSneaker);
        return "redirect:/sneaker/listado";
    }

    
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        Sneaker sneaker = sneakerService.getSneaker(id);
        if (sneaker == null) {
            return "redirect:/";
        }

        
        List<String> tallasLista = new ArrayList<>();
        if (sneaker.getTallas() != null && !sneaker.getTallas().isBlank()) {
            tallasLista = Arrays.stream(sneaker.getTallas().split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
        }

        model.addAttribute("sneaker", sneaker);
        model.addAttribute("tallasLista", tallasLista);
        // imagenesLista usa solo la imagen principal como thumbnail
        List<String> imagenesLista = new ArrayList<>();
        if (sneaker.getRutaImagen() != null && !sneaker.getRutaImagen().isBlank()) {
            imagenesLista.add(sneaker.getRutaImagen());
        }
        model.addAttribute("imagenesLista", imagenesLista);
        return "/sneaker/detalle";
    }
}