package ec.intercampus.intercampus.simulation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tracker")
public record TrackerSettings(boolean enabled, long intervalMs, double speedKmh, long dwellSeconds,
                              double stopMeters, double nearMeters, double fallbackSpeedKmh,
                              double speedFactor, double fenceMeters, long stoppedSeconds) {
    public TrackerSettings {
        if (intervalMs <= 0 || !Double.isFinite(speedKmh) || speedKmh <= 0 || dwellSeconds < 0
                || !Double.isFinite(stopMeters) || stopMeters <= 0 || !Double.isFinite(nearMeters) || nearMeters < stopMeters
                || !Double.isFinite(fallbackSpeedKmh) || fallbackSpeedKmh <= 0
                || !Double.isFinite(speedFactor) || speedFactor <= 0 || speedFactor > 1
                || !Double.isFinite(fenceMeters) || fenceMeters <= 0 || stoppedSeconds <= 0) {
            throw new IllegalArgumentException("La configuración del rastreo no es válida");
        }
    }
}
