package ec.intercampus.intercampus.alert;

import ec.intercampus.intercampus.domain.Coordenada;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AlertRulesTest {
    @Test void geocercaConsideraElSegmentoEntreParadas() {
        var route = List.of(new Coordenada(0, 0), new Coordenada(0, .02));
        assertEquals(0, Geofence.distanceToRoute(new Coordenada(0, .01), route), .01);
        assertTrue(Geofence.distanceToRoute(new Coordenada(.01, .01), route) > 1000);
        assertEquals(0, Geofence.distanceToRoute(new Coordenada(0, 0), List.of(new Coordenada(0, 0))));
    }
    @Test void detencionRequiereTiempoYEstarFueraDeParada() {
        assertTrue(AlertRules.prolongedStop(0, false, Instant.EPOCH, Instant.EPOCH.plusSeconds(60), 60));
        assertFalse(AlertRules.prolongedStop(0, true, Instant.EPOCH, Instant.EPOCH.plusSeconds(60), 60));
        assertFalse(AlertRules.prolongedStop(28, false, Instant.EPOCH, Instant.EPOCH.plusSeconds(60), 60));
        assertFalse(AlertRules.prolongedStop(0, false, Instant.EPOCH, Instant.EPOCH.plusSeconds(59), 60));
    }
}
