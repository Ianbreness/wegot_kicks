package WGK.controller;

import WGK.domain.Sneaker;
import WGK.service.SneakerService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private SneakerService sneakerService;

    @GetMapping("/ver")
    public String verCarrito(HttpSession session, Model model) {
        List<Sneaker> carrito = getCarrito(session);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", calcularTotal(carrito));
        return "/carrito/ver";
    }

    @PostMapping("/agregar/{idSneaker}")
    public String agregar(@PathVariable Integer idSneaker, HttpSession session) {
        Sneaker sneaker = sneakerService.getSneaker(idSneaker);
        if (sneaker != null) {
            getCarrito(session).add(sneaker);
        }
        return "redirect:/";
    }

    @PostMapping("/eliminar/{index}")
    public String eliminar(@PathVariable Integer index, HttpSession session) {
        List<Sneaker> carrito = getCarrito(session);
        if (index >= 0 && index < carrito.size()) {
            carrito.remove((int) index);
        }
        return "redirect:/carrito/ver";
    }

    @PostMapping("/vaciar")
    public String vaciar(HttpSession session) {
        session.removeAttribute("carrito");
        return "redirect:/carrito/ver";
    }

    @SuppressWarnings("unchecked")
    private List<Sneaker> getCarrito(HttpSession session) {
        List<Sneaker> carrito = (List<Sneaker>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    private BigDecimal calcularTotal(List<Sneaker> carrito) {
        return carrito.stream()
                .map(Sneaker::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}