package ec.intercampus.intercampus.domain;

import java.time.Instant;

/**
 * Value object inmutable: ubicacion de un bus en un instante concreto.
 */
public record Posicion(Coordenada coordenada, Instant momento, double speedKmh) {

    public Posicion(Coordenada coordenada, Instant momento) {
        this(coordenada, momento, 0);
    }

    public Posicion {
        if (!Double.isFinite(speedKmh) || speedKmh < 0) {
            throw new DomainException(DomainException.Code.INVALID_INPUT, "La velocidad debe ser finita y no negativa");
        }
        if (coordenada == null) {
            throw new IllegalArgumentException("La coordenada es obligatoria");
        }
        if (momento == null) {
            throw new IllegalArgumentException("El momento es obligatorio");
        }
    }
}
