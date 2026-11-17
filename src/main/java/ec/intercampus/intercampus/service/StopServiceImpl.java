package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.StopDto;
import ec.intercampus.intercampus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static ec.intercampus.intercampus.service.ServiceValidation.*;

@Service
@Transactional(readOnly = true)
public class StopServiceImpl implements StopService {
    private final FavoriteService favorites;
    private final ParadaRepository repository;
    private final RutaRepository routeRepository;
    public StopServiceImpl(ParadaRepository repository, RutaRepository routeRepository, FavoriteService favorites) {
        this.favorites = favorites;
        this.repository = repository;
        this.routeRepository = routeRepository;
    }
    @Override
    public List<StopDto> findByRoute(Long routeId) {
        if (!routeRepository.existsById(id(routeId))) throw missing("Ruta");
        return repository.findByRutaIdOrderByOrdenAsc(routeId).stream().map(DtoMapper::stop).toList();
    }
    private Parada require(Long id) {
        return repository.findById(id(id)).orElseThrow(() -> missing("Parada"));
    }
    @Override
    public StopDto findById(Long id) { return DtoMapper.stop(require(id)); }
    @Override
    @Transactional
    public StopDto create(Long routeId, String name, Coordenada coordinate) {
        name = text(name, "El nombre");
        coordinate(coordinate);
        Ruta route = routeRepository.findById(id(routeId)).orElseThrow(() -> missing("Ruta"));
        Parada stop = new Parada(name, coordinate);
        route.agregarParada(stop);
        return DtoMapper.stop(repository.save(stop));
    }
    @Override
    @Transactional
    public StopDto update(Long id, String name, Coordenada coordinate) {
        Parada stop = require(id);
        name = text(name, "El nombre");
        coordinate(coordinate);
        stop.updateDetails(name, coordinate);
        return DtoMapper.stop(stop);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        Parada stop = require(id);
        favorites.removeStopReferences(id);
        stop.getRuta().removeStop(stop);
        repository.delete(stop);
    }
}
