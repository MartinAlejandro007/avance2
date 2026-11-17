package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.dto.*;
import ec.intercampus.intercampus.domain.*;

public interface RouteService {
    java.util.List<RouteDto> findAll();
    RouteDto findById(Long id);
    RouteDto create(String code, String name);
    RouteDto update(Long id, String code, String name);
    void delete(Long id);
}
