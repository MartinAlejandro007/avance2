package ec.intercampus.intercampus.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** Observación inmutable; conserva el nombre histórico aunque se edite la ruta. */
@Entity
@Table(name = "arrival_records", indexes = @Index(name = "idx_arrival_time", columnList = "actualArrival"))
public class ArrivalRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long routeId;
    @Column(nullable = false) private String routeName;
    @Column(nullable = false) private String busPlate;
    @Column(nullable = false) private String stopName;
    @Column(nullable = false) private Instant expectedArrival;
    @Column(nullable = false) private Instant actualArrival;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'SIMULATED'")
    private ArrivalSource source = ArrivalSource.SIMULATED;
    protected ArrivalRecord() { }
    public ArrivalRecord(Long routeId, String routeName, String busPlate, String stopName, Instant expected, Instant actual) {
        this(routeId, routeName, busPlate, stopName, expected, actual, ArrivalSource.SIMULATED);
    }
    public ArrivalRecord(Long routeId, String routeName, String busPlate, String stopName,
                         Instant expected, Instant actual, ArrivalSource source) {
        this.source = source;
        this.routeId = routeId; this.routeName = routeName; this.busPlate = busPlate;
        this.stopName = stopName; this.expectedArrival = expected; this.actualArrival = actual;
    }
    public String getBusPlate() { return busPlate; }
    public String getStopName() { return stopName; }
    public ArrivalSource getSource() { return source; }
    public Long getRouteId() { return routeId; }
    public String getRouteName() { return routeName; }
    public Instant getExpectedArrival() { return expectedArrival; }
    public Instant getActualArrival() { return actualArrival; }
}
