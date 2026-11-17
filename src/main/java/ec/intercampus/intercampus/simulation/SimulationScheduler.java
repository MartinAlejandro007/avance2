package ec.intercampus.intercampus.simulation;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;

@Component
@ConditionalOnProperty(prefix = "tracker", name = "enabled", havingValue = "true")
public class SimulationScheduler {
    private final PositionSimulator simulator;
    private final ec.intercampus.intercampus.alert.AlertService alerts;
    public SimulationScheduler(PositionSimulator simulator, ec.intercampus.intercampus.alert.AlertService alerts) {
        this.simulator = simulator; this.alerts = alerts;
    }
    @Scheduled(fixedDelayString = "${tracker.interval-ms}", initialDelayString = "${tracker.interval-ms}")
    public void tick() { simulator.advance(); alerts.evaluate(); }
}
