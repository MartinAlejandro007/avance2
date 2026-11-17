package ec.intercampus.intercampus.repository;

import ec.intercampus.intercampus.domain.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    boolean existsByCodigo(String code);
    boolean existsByCodigoAndIdNot(String code, Long id);
}
