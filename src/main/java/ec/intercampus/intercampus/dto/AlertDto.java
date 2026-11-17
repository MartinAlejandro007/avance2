package ec.intercampus.intercampus.dto;

import ec.intercampus.intercampus.domain.AlertType;
import java.time.Instant;

public record AlertDto(Long id, Long busId, String plate, Long routeId, AlertType type,
                       String description, Instant openedAt, Instant resolvedAt) { }
