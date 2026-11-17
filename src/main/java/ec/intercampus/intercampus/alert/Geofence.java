package ec.intercampus.intercampus.alert;

import ec.intercampus.intercampus.domain.Coordenada;
import ec.intercampus.intercampus.simulation.GeoMath;
import java.util.List;

/** Aproximación local de distancia a segmentos, adecuada para recorridos urbanos. */
public final class Geofence {
    private Geofence() { }
    public static double distanceToRoute(Coordenada point, List<Coordenada> stops) {
        if (stops.isEmpty()) return Double.POSITIVE_INFINITY;
        if (stops.size() == 1) return GeoMath.distance(point, stops.get(0));
        double nearest = Double.POSITIVE_INFINITY;
        for (int i = 0; i < stops.size(); i++) {
            nearest = Math.min(nearest, distanceToSegment(point, stops.get(i), stops.get((i + 1) % stops.size())));
        }
        return nearest;
    }
    private static double distanceToSegment(Coordenada point, Coordenada start, Coordenada end) {
        double longitudeScale = Math.cos(Math.toRadians(point.latitud()));
        double x = (end.longitud() - start.longitud()) * longitudeScale;
        double y = end.latitud() - start.latitud();
        double px = (point.longitud() - start.longitud()) * longitudeScale;
        double py = point.latitud() - start.latitud();
        double lengthSquared = x * x + y * y;
        double ratio = lengthSquared == 0 ? 0 : Math.max(0, Math.min(1, (px * x + py * y) / lengthSquared));
        return GeoMath.distance(point, new Coordenada(start.latitud() + (end.latitud() - start.latitud()) * ratio,
                start.longitud() + (end.longitud() - start.longitud()) * ratio));
    }
}
