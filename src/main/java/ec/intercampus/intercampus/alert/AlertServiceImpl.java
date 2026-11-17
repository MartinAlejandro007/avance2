package ec.intercampus.intercampus.alert;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.AlertDto;
import ec.intercampus.intercampus.repository.*;
import ec.intercampus.intercampus.service.PositionService;
import ec.intercampus.intercampus.simulation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AlertServiceImpl implements AlertService {
    private final BusRepository buses;
    private final AlertRepository alerts;
    private final PositionSampleRepository samples;
    private final PositionService positions;
    private final TrackerSettings settings;
    private final Clock clock;
    public AlertServiceImpl(BusRepository buses, AlertRepository alerts, PositionSampleRepository samples,
                            PositionService positions, TrackerSettings settings, Clock clock) {
        this.buses = buses; this.alerts = alerts; this.samples = samples;
        this.positions = positions; this.settings = settings; this.clock = clock;
    }
    @Override @Transactional
    public void evaluate() {
        Instant now = clock.instant();
        for (Bus bus : buses.findAll()) {
            var stops = bus.getRuta().getParadas().stream().map(Parada::coordenada).toList();
            Posicion current = positions.current(bus);
            boolean active = bus.getStatus() == OperationalStatus.ACTIVE && !stops.isEmpty();
            boolean deviated = active && Geofence.distanceToRoute(current.coordenada(), stops) > settings.fenceMeters();
            boolean atStop = stops.stream().anyMatch(stop -> GeoMath.distance(current.coordenada(), stop) <= settings.stopMeters());
            Instant stationarySince = samples.findFirstByBusIdAndSpeedKmhGreaterThanOrderByMomentDescIdDesc(bus.getId(), 1)
                    .or(() -> samples.findFirstByBusIdOrderByMomentAscIdAsc(bus.getId()))
                    .map(PositionSample::getMoment).orElse(current.momento());
            boolean stopped = active && AlertRules.prolongedStop(current.speedKmh(), atStop, stationarySince, now, settings.stoppedSeconds());
            update(bus, AlertType.ROUTE_DEVIATION, deviated, "La unidad está fuera de la tolerancia de su ruta", now);
            update(bus, AlertType.PROLONGED_STOP, stopped, "La unidad permanece detenida fuera de una parada", now);
        }
    }
    private void update(Bus bus, AlertType type, boolean triggered, String message, Instant now) {
        var existing = alerts.findFirstByBusIdAndTypeAndResolvedAtIsNull(bus.getId(), type);
        if (triggered && existing.isEmpty()) alerts.save(new Alert(bus, type, message, now));
        if (!triggered) existing.ifPresent(alert -> alert.resolve(now));
    }
    @Override
    public List<AlertDto> findAll() {
        return alerts.findAllByOrderByOpenedAtDescIdDesc().stream().map(alert -> new AlertDto(alert.getId(),
                alert.getBus().getId(), alert.getBus().getPlaca(), alert.getBus().getRuta().getId(), alert.getType(),
                alert.getDescription(), alert.getOpenedAt(), alert.getResolvedAt())).toList();
    }
}
