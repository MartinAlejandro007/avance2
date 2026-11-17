package ec.intercampus.intercampus.repository;

import ec.intercampus.intercampus.domain.ArrivalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ArrivalRecordRepository extends JpaRepository<ArrivalRecord, Long> {
    List<ArrivalRecord> findTop30BySourceOrderByActualArrivalDescIdDesc(ec.intercampus.intercampus.domain.ArrivalSource source);
    List<ArrivalRecord> findByActualArrivalGreaterThanEqualAndActualArrivalLessThan(Instant from, Instant until);
}
