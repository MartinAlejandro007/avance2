package ec.intercampus.intercampus.repository;

import ec.intercampus.intercampus.domain.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bus b where b.id = :id")
    Optional<Bus> findForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bus b order by b.id")
    List<Bus> findAllForUpdate();

    boolean existsByPlaca(String plate);
    boolean existsByPlacaAndIdNot(String plate, Long id);
    boolean existsByRutaId(Long routeId);
}
