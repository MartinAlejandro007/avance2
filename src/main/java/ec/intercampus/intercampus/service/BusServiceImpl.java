package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.*;
import ec.intercampus.intercampus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static ec.intercampus.intercampus.service.ServiceValidation.*;

@Service
@Transactional(readOnly = true)
public class BusServiceImpl implements BusService {
    private final BusRepository repository;
    private final RutaRepository rutaRepository;

    private final PositionService positions;
    private final JourneyService journeys;
    public BusServiceImpl(BusRepository repository, RutaRepository rutaRepository, PositionService positions, JourneyService journeys) {
        this.journeys = journeys;
        this.positions = positions;
        this.repository = repository;
        this.rutaRepository = rutaRepository;
    }
    @Override
    public List<BusDto> findAll() { return repository.findAll().stream().map(this::toDto).toList(); }
    @Override
    public BusDto findById(Long id) { return toDto(require(id)); }
    private BusDto toDto(Bus bus) { return DtoMapper.bus(bus, positions.current(bus), journeys.describe(bus)); }
    private Bus require(Long id) {
        return repository.findById(id(id)).orElseThrow(() -> missing("Unidad"));
    }
    @Override
    @Transactional
    public BusDto create(String plate, Long routeId, int capacity, Coordenada coordinate) {
        plate = text(plate, "La placa");
        capacity(capacity);
        coordinate(coordinate);
        unique(repository.existsByPlaca(plate));
        if (repository.count() >= 3)
            throw new DomainException(DomainException.Code.INVALID_INPUT, "La flota admite un máximo de 3 buses");
        Ruta route = rutaRepository.findById(id(routeId)).orElseThrow(() -> missing("Ruta"));
        Bus entity = new Bus(plate, route, coordinate, java.time.Instant.now());
        entity.updateDetails(plate, route, capacity);
        repository.save(entity);
        positions.record(entity, entity.posicionActual());
        return toDto(entity);
    }
    @Override
    @Transactional
    public BusDto update(Long id, String plate, Long routeId, int capacity) {
        Bus entity = require(id);
        plate = text(plate, "La placa");
        capacity(capacity);
        unique(repository.existsByPlacaAndIdNot(plate, id));
        Ruta route = rutaRepository.findById(id(routeId)).orElseThrow(() -> missing("Ruta"));
        entity.updateDetails(plate, route, capacity);
        return toDto(entity);
    }
    @Override @Transactional
    public void changeStatus(Long id, OperationalStatus status) {
        if (status == null) throw new DomainException(DomainException.Code.INVALID_INPUT, "El estado es obligatorio");
        require(id).changeStatus(status);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        Bus entity = require(id);
        
        positions.deleteHistory(id);
        repository.delete(entity);
    }
}
