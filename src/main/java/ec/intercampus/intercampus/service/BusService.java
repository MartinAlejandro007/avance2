package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.dto.*;
import ec.intercampus.intercampus.domain.*;

public interface BusService {
    java.util.List<BusDto> findAll();
    BusDto findById(Long id);
    BusDto create(String plate, Long routeId, int capacity, Coordenada coordinate);
    BusDto update(Long id, String plate, Long routeId, int capacity);
    void changeStatus(Long id, OperationalStatus status);
    void delete(Long id);
}
