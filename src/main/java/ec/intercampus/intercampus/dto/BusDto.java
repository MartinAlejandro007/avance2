package ec.intercampus.intercampus.dto;

import ec.intercampus.intercampus.domain.*;

public record BusDto(Long id, String plate, Long routeId, int capacity, Posicion position, OperationalStatus status, JourneyDto journey, boolean manualControl) {}
