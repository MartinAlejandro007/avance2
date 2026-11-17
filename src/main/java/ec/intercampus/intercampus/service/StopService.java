package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.Coordenada;
import ec.intercampus.intercampus.dto.StopDto;
import java.util.List;

public interface StopService {
    List<StopDto> findByRoute(Long routeId);
    StopDto findById(Long id);
    StopDto create(Long routeId, String name, Coordenada coordinate);
    StopDto update(Long id, String name, Coordenada coordinate);
    void delete(Long id);
}
