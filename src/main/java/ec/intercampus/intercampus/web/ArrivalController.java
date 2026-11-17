package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.service.ArrivalService;
import ec.intercampus.intercampus.service.BusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/arrivals")
public class ArrivalController {
    private final ArrivalService arrivals;
    private final BusService buses;
    public ArrivalController(ArrivalService arrivals, BusService buses) {
        this.arrivals = arrivals; this.buses = buses;
    }
    @GetMapping
    public String panel(Model model) {
        model.addAttribute("buses", buses.findAll());
        model.addAttribute("confirmations", arrivals.recentConfirmations());
        return "arrivals";
    }
    @PostMapping("/{busId}/control")
    public String control(@PathVariable Long busId, @RequestParam boolean manual, RedirectAttributes flash) {
        arrivals.changeControl(busId, manual);
        flash.addFlashAttribute("success", manual ? "Control manual activado para este bus" : "El bus vuelve a la simulación automática");
        return "redirect:/arrivals";
    }
    @PostMapping("/{busId}")
    public String confirm(@PathVariable Long busId, @RequestParam Long stopId, RedirectAttributes flash) {
        arrivals.confirm(busId, stopId);
        flash.addFlashAttribute("success", "Llegada registrada. Ya puedes consultar el siguiente campus.");
        return "redirect:/arrivals";
    }
}
