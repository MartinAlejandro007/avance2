package ec.intercampus.intercampus.repository;

import ec.intercampus.intercampus.domain.Parada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParadaRepository extends JpaRepository<Parada, Long> {
    java.util.List<Parada> findByRutaIdOrderByOrdenAsc(Long routeId);
}
