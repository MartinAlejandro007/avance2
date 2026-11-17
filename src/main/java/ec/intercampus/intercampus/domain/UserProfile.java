package ec.intercampus.intercampus.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type;
    @ManyToMany
    @JoinTable(name = "favorite_routes", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "route_id"))
    private java.util.Set<Ruta> favoriteRoutes = new java.util.HashSet<>();
    @ManyToMany
    @JoinTable(name = "favorite_stops", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "stop_id"))
    private java.util.Set<Parada> favoriteStops = new java.util.HashSet<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(16) default 'LIGHT'")
    private VisualTheme theme = VisualTheme.LIGHT;
    public java.util.Set<Ruta> getFavoriteRoutes() { return java.util.Collections.unmodifiableSet(favoriteRoutes); }
    public java.util.Set<Parada> getFavoriteStops() { return java.util.Collections.unmodifiableSet(favoriteStops); }
    public VisualTheme getTheme() { return theme; }
    public void changeTheme(VisualTheme theme) { this.theme = theme; }
    public void favoriteRoute(Ruta route, boolean favorite) { if (favorite) favoriteRoutes.add(route); else favoriteRoutes.remove(route); }
    public void favoriteStop(Parada stop, boolean favorite) { if (favorite) favoriteStops.add(stop); else favoriteStops.remove(stop); }
    protected UserProfile() { }
    public UserProfile(String name, String email, UserType type) { updateDetails(name, email, type); }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserType getType() { return type; }
    public void updateDetails(String name, String email, UserType type) {
        this.name = name;
        this.email = email;
        this.type = type;
    }
}
