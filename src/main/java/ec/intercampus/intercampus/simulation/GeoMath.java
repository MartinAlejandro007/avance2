package ec.intercampus.intercampus.simulation;

import ec.intercampus.intercampus.domain.Coordenada;

/** Operaciones geográficas puras; distancias expresadas en metros. */
public final class GeoMath {
    private static final double EARTH_METERS = 6_371_000;
    private GeoMath() { }
    public static double distance(Coordenada from, Coordenada to) {
        double latitude = Math.toRadians(to.latitud() - from.latitud());
        double longitude = Math.toRadians(to.longitud() - from.longitud());
        double a = Math.pow(Math.sin(latitude / 2), 2)
                + Math.cos(Math.toRadians(from.latitud())) * Math.cos(Math.toRadians(to.latitud()))
                * Math.pow(Math.sin(longitude / 2), 2);
        return EARTH_METERS * 2 * Math.asin(Math.sqrt(Math.min(1, Math.max(0, a))));
    }
    public static Coordenada advance(Coordenada from, Coordenada to, double meters) {
        double distance = distance(from, to);
        if (distance == 0 || meters >= distance) return to;
        double ratio = Math.max(0, meters) / distance;
        return new Coordenada(from.latitud() + (to.latitud() - from.latitud()) * ratio,
                from.longitud() + (to.longitud() - from.longitud()) * ratio);
    }
}
