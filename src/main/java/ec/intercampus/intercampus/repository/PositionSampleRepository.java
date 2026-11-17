package ec.intercampus.intercampus.repository;

import ec.intercampus.intercampus.domain.PositionSample;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PositionSampleRepository extends JpaRepository<PositionSample, Long> {
    Optional<PositionSample> findFirstByBusIdOrderByMomentDescIdDesc(Long busId);
    Optional<PositionSample> findFirstByBusIdAndSpeedKmhGreaterThanOrderByMomentDescIdDesc(Long busId, double speedKmh);
    Optional<PositionSample> findFirstByBusIdOrderByMomentAscIdAsc(Long busId);
    void deleteByBusId(Long busId);
}
