package ec.intercampus.intercampus.dto;

import ec.intercampus.intercampus.domain.*;

public record RouteDto(Long id, String code, String name, java.util.List<StopDto> stops) {
    public RouteDto { stops = java.util.List.copyOf(stops); }
}
