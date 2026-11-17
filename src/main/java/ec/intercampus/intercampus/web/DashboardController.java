package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.domain.OperationalStatus;
import ec.intercampus.intercampus.dto.TrackingSnapshotDto;
import ec.intercampus.intercampus.service.TrackingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DashboardController {
    private final TrackingService tracking;
    public DashboardController(TrackingService tracking) { this.tracking = tracking; }
    @GetMapping("/")
    public String dashboard(Model model) {
        var snapshot = tracking.snapshot(null, null);
        model.addAttribute("snapshot", snapshot);
        model.addAttribute("activeCount", snapshot.buses().stream().filter(b -> b.status() == OperationalStatus.ACTIVE).count());
        model.addAttribute("alertCount", snapshot.alerts().stream().filter(a -> a.resolvedAt() == null).count());
        model.addAttribute("stopCount", snapshot.routes().stream().mapToLong(r -> r.stops().size()).sum());
        return "dashboard";
    }
    @GetMapping("/tracking") @ResponseBody
    public TrackingSnapshotDto tracking(@RequestParam(required = false) Long routeId, @RequestParam(required = false) Long stopId) {
        return tracking.snapshot(routeId, stopId);
    }
}
