package ec.intercampus.intercampus.simulation;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.repository.BusRepository;
import ec.intercampus.intercampus.service.PositionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class PositionSimulatorImpl implements PositionSimulator {
    private final BusRepository buses;
    private final PositionService positions;
    private final TrackerSettings settings;
    private final Clock clock;
    private final ec.intercampus.intercampus.repository.ArrivalRecordRepository arrivals;
    public PositionSimulatorImpl(BusRepository buses, PositionService positions, TrackerSettings settings, Clock clock, ec.intercampus.intercampus.repository.ArrivalRecordRepository arrivals) {
        this.buses = buses; this.positions = positions; this.settings = settings; this.clock = clock; this.arrivals = arrivals;
    }
    @Override @Transactional
    public void advance() {
        Instant now = clock.instant();
        for (Bus bus : buses.findAllForUpdate()) advanceBus(bus, now);
    }
    private void advanceBus(Bus bus, Instant now) {
        if (bus.getStatus() != OperationalStatus.ACTIVE || bus.isManualControl()) return;
        List<Parada> stops = bus.getRuta().getParadas();
        if (stops.isEmpty()) return;
        Posicion current = positions.current(bus);
        if (bus.getDwellUntil() != null && now.isBefore(bus.getDwellUntil())) {
            positions.record(bus, new Posicion(current.coordenada(), now, 0));
            return;
        }
        int index = Math.floorMod(bus.getNextStopIndex(), stops.size());
        Parada target = stops.get(index);
        double distance = GeoMath.distance(current.coordenada(), target.coordenada());
        double step = settings.speedKmh() / 3.6 * settings.intervalMs() / 1000.0;
        if (bus.getExpectedArrival() == null) {
            bus.beginLeg(now.plusMillis((long) (distance / (settings.speedKmh() / 3.6) * 1000)));
        }
        Coordenada coordinate = GeoMath.advance(current.coordenada(), target.coordenada(), step);
        boolean arrived = distance <= step;
        positions.record(bus, new Posicion(coordinate, now, arrived ? 0 : settings.speedKmh()));
        if (arrived) {
            arrivals.save(new ArrivalRecord(bus.getRuta().getId(), bus.getRuta().getNombre(), bus.getPlaca(),
                    target.getNombre(), bus.getExpectedArrival(), now));
            bus.arriveAtStop((index + 1) % stops.size(), now.plusSeconds(settings.dwellSeconds()));
        }
    }
}
