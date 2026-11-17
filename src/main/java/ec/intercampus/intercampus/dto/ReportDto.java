package ec.intercampus.intercampus.dto;

import java.util.List;

public record ReportDto(long arrivals, double punctuality, double averageDelayMinutes, List<ReportRowDto> rows) {
    public ReportDto { rows = List.copyOf(rows); }
}
