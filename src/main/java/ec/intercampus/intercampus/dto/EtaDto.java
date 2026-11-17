package ec.intercampus.intercampus.dto;

public record EtaDto(Long busId, Long stopId, long meters, long minutes, String state, boolean estimatedSpeed) { }
