package ec.intercampus.intercampus.domain;

public enum OperationalStatus {
    ACTIVE("Activo"), MAINTENANCE("Mantenimiento"), OUT_OF_SERVICE("Fuera de servicio");
    private final String label;
    OperationalStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
