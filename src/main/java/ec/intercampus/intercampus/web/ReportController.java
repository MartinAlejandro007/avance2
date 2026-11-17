package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.report.ReportService;
import ec.intercampus.intercampus.service.RouteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.*;

@Controller
public class ReportController {
    private final ReportService reports;
    private final RouteService routes;
    private final Clock clock;
    public ReportController(ReportService reports, RouteService routes, Clock clock) {
        this.reports = reports; this.routes = routes; this.clock = clock;
    }
    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until,
                          @RequestParam(required = false) Long routeId, Model model) {
        LocalDate end = until == null ? LocalDate.now(clock.withZone(ZoneId.of("America/Guayaquil"))) : until;
        LocalDate start = from == null ? end.minusDays(6) : from;
        if (routeId != null) routes.findById(routeId);
        var report = reports.summarize(start, end, routeId);
        model.addAttribute("report", report);
        model.addAttribute("maxDelay", Math.max(1, report.rows().stream().mapToDouble(row -> row.averageDelayMinutes()).max().orElse(0) * 1.15));
        model.addAttribute("routes", routes.findAll());
        model.addAttribute("from", start); model.addAttribute("until", end); model.addAttribute("routeId", routeId);
        return "reports";
    }
}
