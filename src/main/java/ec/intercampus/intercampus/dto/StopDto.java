package ec.intercampus.intercampus.dto;

import ec.intercampus.intercampus.domain.*;

public record StopDto(Long id, String name, Coordenada coordinate, int order, Long routeId) {}
