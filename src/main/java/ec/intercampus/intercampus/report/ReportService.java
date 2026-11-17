package ec.intercampus.intercampus.report;

import ec.intercampus.intercampus.dto.ReportDto;
import java.time.LocalDate;

public interface ReportService {
    ReportDto summarize(LocalDate from, LocalDate until, Long routeId);
}
