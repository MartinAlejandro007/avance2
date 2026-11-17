package ec.intercampus.intercampus.dto;

import java.time.Instant;

public record ArrivalLogDto(String busPlate, String stopName, Instant moment) { }
