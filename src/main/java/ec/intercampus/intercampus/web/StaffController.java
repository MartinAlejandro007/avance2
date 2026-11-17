package ec.intercampus.intercampus.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class StaffController {
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/staff")
    public String staff(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) return "redirect:/fleet";
        if (request.isUserInRole("DRIVER")) return "redirect:/arrivals";
        return "redirect:/login";
    }

    @RequestMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String denied(Model model) {
        model.addAttribute("errorCode", 403);
        model.addAttribute("message", "Tu cuenta no tiene permiso para esta acción. Usa el acceso de administrador para gestionar buses o el de conductor para registrar llegadas.");
        return "error";
    }
}
