package ec.intercampus.intercampus.domain;

/**
 * Value object inmutable que representa una posicion geografica.
 */
public record Coordenada(double latitud, double longitud) {

    public Coordenada {
        if (!Double.isFinite(latitud) || latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90: " + latitud);
        }
        if (!Double.isFinite(longitud) || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180: " + longitud);
        }
    }
}
