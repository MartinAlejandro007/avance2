package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.*;

final class DtoMapper {
    private DtoMapper() { }
    static StopDto stop(Parada value) {
        return new StopDto(value.getId(), value.getNombre(), value.coordenada(), value.getOrden(), value.getRuta().getId());
    }
    static RouteDto route(Ruta value) {
        return new RouteDto(value.getId(), value.getCodigo(), value.getNombre(), value.getParadas().stream().map(DtoMapper::stop).toList());
    }
    static BusDto bus(Bus value, ec.intercampus.intercampus.domain.Posicion position, JourneyDto journey) {
        return new BusDto(value.getId(), value.getPlaca(), value.getRuta().getId(), value.getCapacity(),
                position, value.getStatus(), journey, value.isManualControl());
    }
    static UserDto user(UserProfile value) { return new UserDto(value.getId(), value.getName(), value.getEmail(), value.getType()); }
}
