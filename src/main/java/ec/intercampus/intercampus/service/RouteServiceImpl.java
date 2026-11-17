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
public class RouteServiceImpl implements RouteService {
    private final FavoriteService favorites;
    private final RutaRepository repository;
    private final BusRepository busRepository;

    public RouteServiceImpl(RutaRepository repository, BusRepository busRepository, FavoriteService favorites) {
        this.favorites = favorites;
        this.repository = repository;
        this.busRepository = busRepository;
    }
    @Override
    public List<RouteDto> findAll() { return repository.findAll().stream().map(DtoMapper::route).toList(); }
    @Override
    public RouteDto findById(Long id) { return DtoMapper.route(require(id)); }
    private Ruta require(Long id) {
        return repository.findById(id(id)).orElseThrow(() -> missing("Ruta"));
    }
    @Override
    @Transactional
    public RouteDto create(String code, String name) {
        code = text(code, "El código");
        name = text(name, "El nombre");
        unique(repository.existsByCodigo(code));
        Ruta entity = new Ruta(code, name);
        return DtoMapper.route(repository.save(entity));
    }
    @Override
    @Transactional
    public RouteDto update(Long id, String code, String name) {
        Ruta entity = require(id);
        code = text(code, "El código");
        name = text(name, "El nombre");
        unique(repository.existsByCodigoAndIdNot(code, id));
        entity.updateDetails(code, name);
        return DtoMapper.route(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        Ruta entity = require(id);
        available(busRepository.existsByRutaId(id));
        favorites.removeRouteReferences(id);
        entity.getParadas().forEach(stop -> favorites.removeStopReferences(stop.getId()));
        repository.delete(entity);
    }
}
