package ec.intercampus.intercampus.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recorrido identificado de la universidad, con sus paradas ordenadas.
 */
@Entity
@Table(name = "rutas")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<Parada> paradas = new ArrayList<>();

    protected Ruta() {
        // constructor requerido por JPA
    }

    public Ruta(String codigo, String nombre) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo de la ruta es obligatorio");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la ruta es obligatorio");
        }
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public void updateDetails(String code, String name) {
        this.codigo = code;
        this.nombre = name;
    }

    public void removeStop(Parada stop) {
        paradas.remove(stop);
        for (int i = 0; i < paradas.size(); i++) {
            paradas.get(i).asignarRuta(this, i + 1);
        }
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public List<Parada> getParadas() {
        return Collections.unmodifiableList(paradas);
    }

    /**
     * Agrega una parada al final del recorrido y mantiene su orden.
     */
    public void agregarParada(Parada parada) {
        if (parada == null) {
            throw new IllegalArgumentException("La parada es obligatoria");
        }
        if (!paradas.contains(parada)) {
            paradas.add(parada);
            parada.asignarRuta(this, paradas.size());
        }
    }
}
