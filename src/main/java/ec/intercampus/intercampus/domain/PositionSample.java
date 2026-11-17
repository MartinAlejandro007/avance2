package ec.intercampus.intercampus.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Muestra histórica: solo se crea y consulta, nunca se modifica. */
@Entity
@Table(name = "position_samples", indexes = @Index(name = "idx_position_bus_time", columnList = "bus_id,moment"))
public class PositionSample {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Bus bus;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(nullable = false) private Instant moment;
    @Column(nullable = false) private double speedKmh;
    protected PositionSample() { }
    public PositionSample(Bus bus, Posicion position) {
        this.bus = bus;
        this.latitude = position.coordenada().latitud();
        this.longitude = position.coordenada().longitud();
        this.moment = position.momento();
        this.speedKmh = position.speedKmh();
    }
    public Posicion position() { return new Posicion(new Coordenada(latitude, longitude), moment, speedKmh); }
    public Instant getMoment() { return moment; }
}
