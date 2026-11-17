package ec.intercampus.intercampus.simulation;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.EtaDto;

public final class EtaCalculator {
    private EtaCalculator() { }
    public static EtaDto calculate(Long busId, Long stopId, Posicion position, Coordenada target, TrackerSettings settings) {
        return calculate(busId, stopId, position, target, settings, 0);
    }
    public static EtaDto calculate(Long busId, Long stopId, Posicion position, Coordenada target,
                                   TrackerSettings settings, long remainingPauseMillis) {
        double distance = GeoMath.distance(position.coordenada(), target);
        boolean fallback = position.speedKmh() <= 0;
        double speed = (fallback ? settings.fallbackSpeedKmh() : position.speedKmh()) * settings.speedFactor();
        String state = distance <= settings.stopMeters() ? "En Parada" : distance <= settings.nearMeters() ? "Próximo" : "En ruta";
        double travelMinutes = distance <= settings.stopMeters() ? 0 : distance / (speed * 1000 / 60);
        long minutes = (long) Math.ceil(travelMinutes + Math.max(0, remainingPauseMillis) / 60000.0);
        return new EtaDto(busId, stopId, Math.round(distance), minutes, state, fallback);
    }
}
