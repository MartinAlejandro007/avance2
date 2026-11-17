package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fleet")
public class FleetController {
    private final BusService buses;
    private final RouteService routes;
    public FleetController(BusService buses, RouteService routes) {
        this.buses = buses; this.routes = routes;
    }
    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Model model) {
        model.addAttribute("buses", buses.findAll()); model.addAttribute("routes", routes.findAll());
        model.addAttribute("statuses", OperationalStatus.values());
        if (edit != null) model.addAttribute("editing", buses.findById(edit));
        return "fleet";
    }
    @PostMapping
    public String create(@RequestParam String plate, @RequestParam Long routeId, @RequestParam int capacity,
                         @RequestParam double latitude,
                         @RequestParam double longitude, RedirectAttributes flash) {
        buses.create(plate, routeId, capacity, new Coordenada(latitude, longitude));
        flash.addFlashAttribute("success", "Unidad registrada"); return "redirect:/fleet";
    }
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String plate, @RequestParam Long routeId,
                         @RequestParam int capacity, RedirectAttributes flash) {
        buses.update(id, plate, routeId, capacity);
        flash.addFlashAttribute("success", "Unidad actualizada"); return "redirect:/fleet";
    }
    @PostMapping("/{id}/status")
    public String status(@PathVariable Long id, @RequestParam OperationalStatus status, RedirectAttributes flash) {
        buses.changeStatus(id, status); flash.addFlashAttribute("success", "Estado operativo actualizado"); return "redirect:/fleet";
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        buses.delete(id); flash.addFlashAttribute("success", "Unidad eliminada"); return "redirect:/fleet";
    }
}
