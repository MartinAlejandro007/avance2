package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.ArrivalLogDto;
import ec.intercampus.intercampus.repository.*;
import ec.intercampus.intercampus.simulation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;
import static ec.intercampus.intercampus.service.ServiceValidation.*;

@Service
@Transactional(readOnly = true)
public class ArrivalServiceImpl implements ArrivalService {
    private final BusRepository buses;
    private final ArrivalRecordRepository arrivals;
    private final PositionService positions;
    private final TrackerSettings settings;
    private final Clock clock;

    public ArrivalServiceImpl(BusRepository buses, ArrivalRecordRepository arrivals,
                              PositionService positions, TrackerSettings settings, Clock clock) {
        this.buses = buses; this.arrivals = arrivals; this.positions = positions;
        this.settings = settings; this.clock = clock;
    }

    private Bus requireActive(Long busId) {
        var bus = buses.findForUpdate(id(busId)).orElseThrow(() -> missing("Unidad"));
        if (bus.getStatus() != OperationalStatus.ACTIVE)
            throw new DomainException(DomainException.Code.INVALID_INPUT, "Solo una unidad activa puede registrar llegadas");
        if (bus.getRuta().getParadas().isEmpty())
            throw new DomainException(DomainException.Code.INVALID_INPUT, "La unidad necesita una ruta con paradas");
        return bus;
    }

    @Override @Transactional
    public void changeControl(Long busId, boolean manual) {
        var bus = buses.findForUpdate(id(busId)).orElseThrow(() -> missing("Unidad"));
        if (bus.isManualControl() == manual) return;
        if (manual) {
            requireActive(busId);
            var current = positions.current(bus);
            positions.record(bus, new Posicion(current.coordenada(), clock.instant(), 0));
            if (bus.getExpectedArrival() == null) planNextLeg(bus, current.coordenada(), clock.instant());
        }
        bus.changeControl(manual);
    }

    @Override @Transactional
    public void confirm(Long busId, Long stopId) {
        var bus = requireActive(busId);
        if (!bus.isManualControl())
            throw new DomainException(DomainException.Code.INVALID_INPUT, "Activa primero el control manual de este bus");
        var stops = bus.getRuta().getParadas();
        int index = Math.floorMod(bus.getNextStopIndex(), stops.size());
        var stop = stops.get(index);
        if (!stop.getId().equals(id(stopId)))
            throw new DomainException(DomainException.Code.INVALID_INPUT, "La parada ya cambió o no es la siguiente. Actualiza el panel antes de confirmar");
        Instant now = clock.instant();
        if (bus.getExpectedArrival() == null) planNextLeg(bus, positions.current(bus).coordenada(), now);
        arrivals.save(new ArrivalRecord(bus.getRuta().getId(), bus.getRuta().getNombre(), bus.getPlaca(),
                stop.getNombre(), bus.getExpectedArrival(), now, ArrivalSource.DRIVER_CONFIRMED));
        positions.record(bus, new Posicion(stop.coordenada(), now, 0));
        bus.arriveAtStop((index + 1) % stops.size(), now.plusSeconds(settings.dwellSeconds()));
        planNextLeg(bus, stop.coordenada(), now);
    }

    private void planNextLeg(Bus bus, Coordenada coordinate, Instant now) {
        var stops = bus.getRuta().getParadas();
        var target = stops.get(Math.floorMod(bus.getNextStopIndex(), stops.size()));
        double distance = GeoMath.distance(coordinate, target.coordenada());
        long travelMillis = (long) Math.ceil(distance / (settings.speedKmh() / 3.6) * 1000);
        Instant departure = bus.getDwellUntil() != null && bus.getDwellUntil().isAfter(now) ? bus.getDwellUntil() : now;
        bus.beginLeg(departure.plusMillis(travelMillis));
    }

    @Override
    public List<ArrivalLogDto> recentConfirmations() {
        return arrivals.findTop30BySourceOrderByActualArrivalDescIdDesc(ArrivalSource.DRIVER_CONFIRMED).stream()
                .map(a -> new ArrivalLogDto(a.getBusPlate(), a.getStopName(), a.getActualArrival())).toList();
    }
}
