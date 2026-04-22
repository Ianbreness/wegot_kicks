package WGK.controller;

import WGK.domain.Sneaker;
import WGK.service.MarcaService;
import WGK.service.SneakerService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

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
            @RequestParam("imagenFile") MultipartFile imagenPrincipal,
            @RequestParam(value = "imagenesExtra", required = false)
                List<MultipartFile> imagenesExtra) throws Exception {
        sneakerService.save(sneaker, imagenPrincipal, imagenesExtra);
        return "redirect:/sneaker/listado";
    }

    // ── Eliminar ─────────────────────────────────────────────────────────
    @PostMapping("/eliminar")
    public String eliminar(@RequestParam("idSneaker") Integer idSneaker) throws Exception {
        sneakerService.delete(idSneaker);
        return "redirect:/sneaker/listado";
    }

    // ── DETALLE público ──────────────────────────────────────────────────
    /**
     * Muestra la página de detalle de un sneaker.
     * URL pública: GET /sneaker/detalle/{id}
     * Se abre en nueva pestaña desde el catálogo.
     */
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        Sneaker sneaker = sneakerService.getSneaker(id);
        if (sneaker == null) {
            return "redirect:/";
        }

        // Convertir el string de tallas "38,39,40,41,42" en una List<String>
        List<String> tallasLista = Collections.emptyList();
        if (sneaker.getTallas() != null && !sneaker.getTallas().isBlank()) {
            tallasLista = Arrays.stream(sneaker.getTallas().split(","))
                    .map(String::trim)
                    .filter(t -> !t.isEmpty())
                    .collect(Collectors.toList());
        }

        // Convertir el string de imágenes adicionales en List<String>
        // Incluye la imagen principal como primer thumbnail
        List<String> imagenesLista = new java.util.ArrayList<>();
        if (sneaker.getRutaImagen() != null && !sneaker.getRutaImagen().isBlank()) {
            imagenesLista.add(sneaker.getRutaImagen()); // siempre primero
        }
        if (sneaker.getImagenes() != null && !sneaker.getImagenes().isBlank()) {
            Arrays.stream(sneaker.getImagenes().split(","))
                    .map(String::trim)
                    .filter(img -> !img.isEmpty())
                    .forEach(imagenesLista::add);
        }

        model.addAttribute("sneaker", sneaker);
        model.addAttribute("tallasLista", tallasLista);
        model.addAttribute("imagenesLista", imagenesLista);
        return "/sneaker/detalle";
    }
}