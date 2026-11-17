package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.dto.TrackingSnapshotDto;

public interface TrackingService {
    TrackingSnapshotDto snapshot(Long routeId, Long stopId);
}
