package ec.intercampus.intercampus.dto;

public record ReportRowDto(Long routeId, String routeName, String timeSlot, long arrivals,
                           double punctuality, double averageDelayMinutes) { }
