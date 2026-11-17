package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.*;
import ec.intercampus.intercampus.simulation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
@Transactional(readOnly = true)
public class JourneyServiceImpl implements JourneyService {
    private final PositionService positions;
    private final TrackerSettings settings;
    private final Clock clock;

    public JourneyServiceImpl(PositionService positions, TrackerSettings settings, Clock clock) {
        this.positions = positions; this.settings = settings; this.clock = clock;
    }

    @Override
    public JourneyDto describe(Bus bus) {
        if (bus.getStatus() != OperationalStatus.ACTIVE)
            return new JourneyDto(bus.getStatus().getLabel(), null, null);
        var stops = bus.getRuta().getParadas();
        if (stops.isEmpty()) return new JourneyDto("Sin paradas asignadas", null, null);
        int nextIndex = Math.floorMod(bus.getNextStopIndex(), stops.size());
        var next = stops.get(nextIndex);
        var position = positions.current(bus);
        long remainingPause = bus.getDwellUntil() == null ? 0
                : Math.max(0, Duration.between(clock.instant(), bus.getDwellUntil()).toMillis());
        var arrival = EtaCalculator.calculate(bus.getId(), next.getId(), position, next.coordenada(), settings, remainingPause);
        String state = "En camino a " + next.getNombre();
        if (remainingPause > 0) {
            var previous = stops.get(Math.floorMod(nextIndex - 1, stops.size()));
            state = "Llegó a " + previous.getNombre();
        }
        return new JourneyDto(state, next.getNombre(), arrival);
    }
}
