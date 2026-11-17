package ec.intercampus.intercampus.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class Alert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private Bus bus;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AlertType type;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private Instant openedAt;
    private Instant resolvedAt;
    protected Alert() { }
    public Alert(Bus bus, AlertType type, String description, Instant openedAt) {
        this.bus = bus; this.type = type; this.description = description; this.openedAt = openedAt;
    }
    public Long getId() { return id; }
    public Bus getBus() { return bus; }
    public AlertType getType() { return type; }
    public String getDescription() { return description; }
    public Instant getOpenedAt() { return openedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void resolve(Instant moment) { this.resolvedAt = moment; }
}
