package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.EtaDto;
import ec.intercampus.intercampus.repository.*;
import ec.intercampus.intercampus.simulation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static ec.intercampus.intercampus.service.ServiceValidation.*;

@Service
@Transactional(readOnly = true)
public class EtaServiceImpl implements EtaService {
    private final BusRepository buses;
    private final ParadaRepository stops;
    private final PositionService positions;
    private final TrackerSettings settings;
    public EtaServiceImpl(BusRepository buses, ParadaRepository stops, PositionService positions, TrackerSettings settings) {
        this.buses = buses; this.stops = stops; this.positions = positions; this.settings = settings;
    }
    @Override
    public EtaDto calculate(Long busId, Long stopId) {
        Bus bus = buses.findById(id(busId)).orElseThrow(() -> missing("Unidad"));
        Parada stop = stops.findById(id(stopId)).orElseThrow(() -> missing("Parada"));
        if (!bus.getRuta().getId().equals(stop.getRuta().getId()))
            throw new DomainException(DomainException.Code.INVALID_INPUT, "La parada no pertenece a la ruta de la unidad");
        return EtaCalculator.calculate(busId, stopId, positions.current(bus), stop.coordenada(), settings);
    }
}
