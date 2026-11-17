package ec.intercampus.intercampus.repository;

import ec.intercampus.intercampus.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByOrderByOpenedAtDescIdDesc();
    Optional<Alert> findFirstByBusIdAndTypeAndResolvedAtIsNull(Long busId, AlertType type);
}
