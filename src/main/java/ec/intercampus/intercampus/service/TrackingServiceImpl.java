package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.*;
import ec.intercampus.intercampus.alert.AlertService;
import ec.intercampus.intercampus.simulation.TrackerSettings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrackingServiceImpl implements TrackingService {
    private final RouteService routes;
    private final BusService buses;
    private final AlertService alerts;
    private final StopService stops;
    private final EtaService eta;
    private final TrackerSettings settings;
    private final Clock clock;
    public TrackingServiceImpl(RouteService routes, BusService buses, AlertService alerts, StopService stops,
                               EtaService eta, TrackerSettings settings, Clock clock) {
        this.routes = routes; this.buses = buses; this.alerts = alerts; this.stops = stops;
        this.eta = eta; this.settings = settings; this.clock = clock;
    }
    @Override
    public TrackingSnapshotDto snapshot(Long routeId, Long stopId) {
        List<RouteDto> selectedRoutes = routeId == null ? routes.findAll() : List.of(routes.findById(routeId));
        if (stopId != null) {
            StopDto stop = stops.findById(stopId);
            if (routeId == null || !stop.routeId().equals(routeId))
                throw new DomainException(DomainException.Code.INVALID_INPUT, "Selecciona una parada de esta ruta");
        }
        var selectedBuses = buses.findAll().stream().filter(b -> routeId == null || b.routeId().equals(routeId)).toList();
        var selectedAlerts = alerts.findAll().stream().filter(a -> routeId == null || a.routeId().equals(routeId)).toList();
        var arrivals = stopId == null ? List.<EtaDto>of() : selectedBuses.stream().filter(b -> b.status() == OperationalStatus.ACTIVE)
                .map(b -> eta.calculate(b.id(), stopId)).toList();
        return new TrackingSnapshotDto(selectedRoutes, selectedBuses, selectedAlerts, arrivals, clock.instant(), settings.enabled());
    }
}
