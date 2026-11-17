package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.repository.PositionSampleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PositionServiceImpl implements PositionService {
    private final PositionSampleRepository repository;
    public PositionServiceImpl(PositionSampleRepository repository) { this.repository = repository; }
    @Override
    public Posicion current(Bus bus) {
        // Los campos antiguos permiten leer buses creados antes de incorporar el historial.
        return repository.findFirstByBusIdOrderByMomentDescIdDesc(bus.getId())
                .map(PositionSample::position).orElseGet(bus::posicionActual);
    }
    @Override @Transactional
    public void record(Bus bus, Posicion position) { repository.save(new PositionSample(bus, position)); }
    @Override @Transactional
    public void deleteHistory(Long busId) { repository.deleteByBusId(busId); }
}
