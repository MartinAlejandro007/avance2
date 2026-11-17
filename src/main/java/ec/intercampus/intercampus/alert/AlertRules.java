package ec.intercampus.intercampus.alert;

import java.time.*;

public final class AlertRules {
    private AlertRules() { }
    public static boolean prolongedStop(double speedKmh, boolean atStop, Instant stationarySince, Instant now, long thresholdSeconds) {
        return speedKmh <= 1 && !atStop && !stationarySince.isAfter(now)
                && Duration.between(stationarySince, now).getSeconds() >= thresholdSeconds;
    }
}
