package ec.intercampus.intercampus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Bus universitario: pertenece a una ruta y guarda su ultima ubicacion conocida.
 */
@Entity
@Table(name = "buses")
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String placa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;

    @Column(nullable = false)
    private Instant ultimaActualizacion;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int capacity;

    public int getCapacity() { return capacity; }

    public void updateDetails(String plate, Ruta route, int capacity) {
        if (this.ruta != route) resetJourney();
        this.placa = plate;
        this.ruta = route;
        this.capacity = capacity;
    }

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int nextStopIndex;
    private Instant dwellUntil;
    private Instant expectedArrival;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean manualControl;
    public boolean isManualControl() { return manualControl; }
    public void changeControl(boolean manualControl) { this.manualControl = manualControl; }

    public int getNextStopIndex() { return nextStopIndex; }
    public Instant getDwellUntil() { return dwellUntil; }
    public Instant getExpectedArrival() { return expectedArrival; }
    public void beginLeg(Instant expectedArrival) { this.expectedArrival = expectedArrival; }
    public void arriveAtStop(int nextIndex, Instant dwellUntil) {
        this.nextStopIndex = nextIndex;
        this.dwellUntil = dwellUntil;
        this.expectedArrival = null;
    }
    public void resetJourney() { this.nextStopIndex = 0; this.dwellUntil = null; this.expectedArrival = null; }

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) default 'ACTIVE'")
    private OperationalStatus status = OperationalStatus.ACTIVE;

    @jakarta.persistence.OneToMany(mappedBy = "bus", cascade = jakarta.persistence.CascadeType.REMOVE)
    private java.util.List<Alert> alerts = new java.util.ArrayList<>();

    public OperationalStatus getStatus() { return status; }
    public void changeStatus(OperationalStatus status) { this.status = status; }

    protected Bus() {
        // constructor requerido por JPA
    }

    public Bus(String placa, Ruta ruta, Coordenada ubicacionInicial, Instant momento) {
        if (placa == null || placa.isBlank()) {
            throw new IllegalArgumentException("La placa del bus es obligatoria");
        }
        if (ruta == null) {
            throw new IllegalArgumentException("La ruta del bus es obligatoria");
        }
        if (ubicacionInicial == null) {
            throw new IllegalArgumentException("La ubicacion inicial es obligatoria");
        }
        if (momento == null) {
            throw new IllegalArgumentException("El momento es obligatorio");
        }
        this.placa = placa;
        this.ruta = ruta;
        this.latitud = ubicacionInicial.latitud();
        this.longitud = ubicacionInicial.longitud();
        this.ultimaActualizacion = momento;
    }

    public Long getId() {
        return id;
    }

    public String getPlaca() {
        return placa;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public Coordenada coordenadaActual() {
        return new Coordenada(latitud, longitud);
    }

    public Posicion posicionActual() {
        return new Posicion(coordenadaActual(), ultimaActualizacion);
    }

    /**
     * Actualiza la ubicacion a partir de una posicion completa e inmutable.
     */
    public void actualizarUbicacion(Posicion posicion) {
        if (posicion == null) {
            throw new IllegalArgumentException("La posicion es obligatoria");
        }
        this.latitud = posicion.coordenada().latitud();
        this.longitud = posicion.coordenada().longitud();
        this.ultimaActualizacion = posicion.momento();
    }
}
