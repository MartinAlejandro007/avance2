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

/**
 * Punto fisico del recorrido, con coordenadas y posicion dentro de la ruta.
 */
@Entity
@Table(name = "paradas")
public class Parada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private double latitud;

    @Column(nullable = false)
    private double longitud;

    @Column(nullable = false)
    private int orden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    protected Parada() {
        // constructor requerido por JPA
    }

    public Parada(String nombre, Coordenada coordenada) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la parada es obligatorio");
        }
        if (coordenada == null) {
            throw new IllegalArgumentException("La coordenada de la parada es obligatoria");
        }
        this.nombre = nombre;
        this.latitud = coordenada.latitud();
        this.longitud = coordenada.longitud();
    }

    public void updateDetails(String name, Coordenada coordinate) {
        this.nombre = name;
        this.latitud = coordinate.latitud();
        this.longitud = coordinate.longitud();
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getOrden() {
        return orden;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public Coordenada coordenada() {
        return new Coordenada(latitud, longitud);
    }

    /**
     * Establece la ruta y el orden. Lo invoca Ruta.agregarParada().
     */
    public void asignarRuta(Ruta ruta, int orden) {
        if (ruta == null) {
            throw new IllegalArgumentException("La ruta es obligatoria");
        }
        if (orden < 1) {
            throw new IllegalArgumentException("El orden de la parada empieza en 1");
        }
        this.ruta = ruta;
        this.orden = orden;
    }
}
