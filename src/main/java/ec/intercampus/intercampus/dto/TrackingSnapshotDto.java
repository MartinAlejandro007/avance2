package ec.intercampus.intercampus.dto;

import java.time.Instant;
import java.util.List;

public record TrackingSnapshotDto(List<RouteDto> routes, List<BusDto> buses, List<AlertDto> alerts,
                                  List<EtaDto> arrivals, Instant updatedAt, boolean simulationEnabled) {
    public TrackingSnapshotDto {
        routes = List.copyOf(routes); buses = List.copyOf(buses); alerts = List.copyOf(alerts); arrivals = List.copyOf(arrivals);
    }
}
