package ec.intercampus.intercampus.report;

import ec.intercampus.intercampus.domain.ArrivalRecord;
import ec.intercampus.intercampus.dto.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public final class PunctualityCalculator {
    private PunctualityCalculator() { }
    public static double delayMinutes(ArrivalRecord arrival) {
        return Math.max(0, Duration.between(arrival.getExpectedArrival(), arrival.getActualArrival()).toMillis() / 60000.0);
    }
    public static String timeSlot(Instant moment) {
        int hour = moment.atZone(ZoneId.of("America/Guayaquil")).getHour();
        return hour >= 5 && hour < 12 ? "Mañana" : hour >= 12 && hour < 18 ? "Tarde" : "Noche";
    }
    public static ReportDto summarize(List<ArrivalRecord> arrivals, double toleranceMinutes) {
        record Group(Long routeId, String timeSlot) { }
        var groups = arrivals.stream().collect(Collectors.groupingBy(a -> new Group(a.getRouteId(), timeSlot(a.getActualArrival()))));
        var rows = groups.entrySet().stream().map(entry -> {
            var records = entry.getValue();
            return new ReportRowDto(entry.getKey().routeId(), records.get(0).getRouteName(), entry.getKey().timeSlot(),
                    records.size(), punctuality(records, toleranceMinutes), average(records));
        }).sorted(Comparator.comparing(ReportRowDto::routeName).thenComparing(ReportRowDto::timeSlot)).toList();
        return new ReportDto(arrivals.size(), punctuality(arrivals, toleranceMinutes), average(arrivals), rows);
    }
    private static double punctuality(List<ArrivalRecord> records, double tolerance) {
        return records.isEmpty() ? 0 : records.stream().filter(a -> delayMinutes(a) <= tolerance).count() * 100.0 / records.size();
    }
    private static double average(List<ArrivalRecord> records) {
        return records.stream().mapToDouble(PunctualityCalculator::delayMinutes).average().orElse(0);
    }
}
