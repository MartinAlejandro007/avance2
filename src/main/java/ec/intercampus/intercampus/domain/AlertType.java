package ec.intercampus.intercampus.domain;

public enum AlertType {
    ROUTE_DEVIATION("Desvío de ruta"), PROLONGED_STOP("Detención prolongada");
    private final String label;
    AlertType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
