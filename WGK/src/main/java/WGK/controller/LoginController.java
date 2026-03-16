package WGK.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "/login";
    }

    @GetMapping("/acceso_denegado")
    public String accesoDenegado() {
        return "/acceso_denegado";
    }
}