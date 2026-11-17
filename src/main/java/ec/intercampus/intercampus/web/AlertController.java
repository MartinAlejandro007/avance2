package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.alert.AlertService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AlertController {
    private final AlertService alerts;
    public AlertController(AlertService alerts) { this.alerts = alerts; }
    @GetMapping("/alerts")
    public String list(Model model) {
        model.addAttribute("alerts", alerts.findAll()); return "alerts";
    }
}
