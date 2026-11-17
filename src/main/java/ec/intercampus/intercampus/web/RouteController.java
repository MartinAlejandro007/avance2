package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/routes")
public class RouteController {
    private final RouteService routes;
    private final StopService stops;
    private final TrackingService tracking;
    public RouteController(RouteService routes, StopService stops, TrackingService tracking) {
        this.routes = routes; this.stops = stops; this.tracking = tracking;
    }
    @GetMapping
    public String list(@RequestParam(required = false) Long edit, Model model) {
        model.addAttribute("routes", routes.findAll());
        if (edit != null) model.addAttribute("editing", routes.findById(edit));
        return "routes";
    }
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, @RequestParam(required = false) Long stopId,
                         @RequestParam(required = false) Long editStop, Model model) {
        var route = routes.findById(id);
        Long selectedStop = stopId != null ? stopId : route.stops().isEmpty() ? null : route.stops().get(0).id();
        model.addAttribute("route", route);
        model.addAttribute("snapshot", tracking.snapshot(id, selectedStop));
        model.addAttribute("selectedStop", selectedStop);
        if (editStop != null) model.addAttribute("editing", requireStop(id, editStop));
        return "route-detail";
    }
    private ec.intercampus.intercampus.dto.StopDto requireStop(Long routeId, Long stopId) {
        var stop = stops.findById(stopId);
        if (!stop.routeId().equals(routeId)) throw new DomainException(DomainException.Code.INVALID_INPUT, "La parada pertenece a otra ruta");
        return stop;
    }
    @PostMapping
    public String create(@RequestParam String code, @RequestParam String name, RedirectAttributes flash) {
        var route = routes.create(code, name);
        flash.addFlashAttribute("success", "Ruta creada. Ahora puedes agregar sus paradas.");
        return "redirect:/routes/" + route.id();
    }
    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @RequestParam String code, @RequestParam String name, RedirectAttributes flash) {
        routes.update(id, code, name); flash.addFlashAttribute("success", "Ruta actualizada"); return "redirect:/routes";
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        routes.delete(id); flash.addFlashAttribute("success", "Ruta eliminada"); return "redirect:/routes";
    }
    @PostMapping("/{id}/stops")
    public String createStop(@PathVariable Long id, @RequestParam String name, @RequestParam double latitude,
                             @RequestParam double longitude, RedirectAttributes flash) {
        stops.create(id, name, new Coordenada(latitude, longitude));
        flash.addFlashAttribute("success", "Parada agregada al final del recorrido"); return "redirect:/routes/" + id;
    }
    @PostMapping("/{id}/stops/{stopId}")
    public String updateStop(@PathVariable Long id, @PathVariable Long stopId, @RequestParam String name,
                             @RequestParam double latitude, @RequestParam double longitude, RedirectAttributes flash) {
        requireStop(id, stopId); stops.update(stopId, name, new Coordenada(latitude, longitude));
        flash.addFlashAttribute("success", "Parada actualizada"); return "redirect:/routes/" + id;
    }
    @PostMapping("/{id}/stops/{stopId}/delete")
    public String deleteStop(@PathVariable Long id, @PathVariable Long stopId, RedirectAttributes flash) {
        requireStop(id, stopId); stops.delete(stopId);
        flash.addFlashAttribute("success", "Parada eliminada y recorrido reordenado"); return "redirect:/routes/" + id;
    }
}
