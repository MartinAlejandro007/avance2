package ec.intercampus.intercampus.report;

import ec.intercampus.intercampus.domain.DomainException;
import ec.intercampus.intercampus.dto.ReportDto;
import ec.intercampus.intercampus.repository.ArrivalRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private final ArrivalRecordRepository arrivals;
    private final double toleranceMinutes;
    public ReportServiceImpl(ArrivalRecordRepository arrivals, @Value("${reports.punctuality-tolerance-minutes:2}") double toleranceMinutes) {
        if (!Double.isFinite(toleranceMinutes) || toleranceMinutes < 0) throw new IllegalArgumentException("La tolerancia de puntualidad no es válida");
        this.arrivals = arrivals; this.toleranceMinutes = toleranceMinutes;
    }
    @Override
    public ReportDto summarize(LocalDate from, LocalDate until, Long routeId) {
        if (from == null || until == null || until.isBefore(from) || until.isAfter(from.plusYears(1)))
            throw new DomainException(DomainException.Code.INVALID_INPUT, "Selecciona un intervalo válido de hasta un año");
        ZoneId zone = ZoneId.of("America/Guayaquil");
        var records = arrivals.findByActualArrivalGreaterThanEqualAndActualArrivalLessThan(from.atStartOfDay(zone).toInstant(), until.plusDays(1).atStartOfDay(zone).toInstant());
        return PunctualityCalculator.summarize(records.stream().filter(a -> routeId == null || routeId.equals(a.getRouteId())).toList(), toleranceMinutes);
    }
}
