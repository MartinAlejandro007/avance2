package ec.intercampus.intercampus;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.repository.*;
import ec.intercampus.intercampus.service.*;
import ec.intercampus.intercampus.simulation.TrackerSettings;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

/** Escenario de tres campus con coordenadas ficticias y ubicación simulada. */
@Component
@ConditionalOnProperty(name = "demo.seed-enabled", havingValue = "true", matchIfMissing = true)
public class DatosIniciales implements CommandLineRunner {
    private final RutaRepository routes;
    private final BusRepository buses;
    private final UserProfileRepository users;
    private final PositionService positions;
    private final RouteService routeService;
    private final StopService stopService;
    private final BusService busService;
    private final TrackerSettings settings;
    private final Clock clock;
    public DatosIniciales(RutaRepository routes, BusRepository buses, UserProfileRepository users,
                         PositionService positions, RouteService routeService, StopService stopService,
                         BusService busService, TrackerSettings settings, Clock clock) {
        this.routes = routes; this.buses = buses; this.users = users; this.positions = positions;
        this.routeService = routeService; this.stopService = stopService; this.busService = busService;
        this.settings = settings; this.clock = clock;
    }
    @Override @Transactional
    public void run(String... args) {
        if (routes.count() > 0) {
            updateFirstDemo();
            updatePreviousDemo();
            return;
        }
        var route = new Ruta("IC-01", "Jipijapa · UDLA Granados · UDLA Park");
        route.agregarParada(new Parada("Jipijapa", new Coordenada(-.1600, -78.4700)));
        route.agregarParada(new Parada("UDLA Granados", new Coordenada(-.1682, -78.4702)));
        route.agregarParada(new Parada("UDLA Park", new Coordenada(-.1450, -78.4590)));
        routes.save(route);
        for (int i = 0; i < 3; i++) {
            var bus = new Bus("UDLA-0" + (i + 1), route, route.getParadas().get(i).coordenada(), clock.instant());
            bus.updateDetails(bus.getPlaca(), route, 40);
            buses.save(bus);
            startAtCampus(bus, i);
        }
        if (!users.existsByEmail("estudiante@udla.edu.ec")) {
            var student = new UserProfile("Estudiante de demostración", "estudiante@udla.edu.ec", UserType.STUDENT);
            student.favoriteRoute(route, true); student.favoriteStop(route.getParadas().get(0), true); users.save(student);
        }
    }

    private void startAtCampus(Bus bus, int index) {
        var stops = bus.getRuta().getParadas();
        bus.resetJourney();
        bus.arriveAtStop((index + 1) % stops.size(), clock.instant().plusSeconds(settings.dwellSeconds()));
        positions.record(bus, new Posicion(stops.get(index).coordenada(), clock.instant(), 0));
    }

    /** Solo actualiza el escenario anterior conocido; respeta recorridos personalizados. */
    private void updateFirstDemo() {
        var existingRoutes = routes.findAll();
        var existingBuses = buses.findAll();
        if (existingRoutes.size() != 2 || existingBuses.size() != 2) return;
        var north = existingRoutes.stream().filter(r -> r.getCodigo().equals("R-01") && r.getNombre().equals("Ruta Norte")).findFirst();
        var south = existingRoutes.stream().filter(r -> r.getCodigo().equals("R-02") && r.getNombre().equals("Ruta Sur")).findFirst();
        if (north.isEmpty() || south.isEmpty()) return;
        if (!north.get().getParadas().stream().map(Parada::getNombre).toList().equals(List.of(
                "Terminal Terrestre", "Facultad de Ingenieria", "Campus Norte"))) return;
        if (!south.get().getParadas().stream().map(Parada::getNombre).toList().equals(List.of(
                "Terminal Terrestre", "Facultad de Derecho", "Campus Sur"))) return;
        if (!existingBuses.stream().map(Bus::getPlaca).collect(java.util.stream.Collectors.toSet())
                .equals(Set.of("BUS-001", "BUS-002"))) return;
        var route = north.get();
        var retained = existingBuses.stream().sorted(Comparator.comparing(Bus::getPlaca)).toList();
        for (int i = 0; i < retained.size(); i++)
            retained.get(i).updateDetails("UDLA-0" + (i + 1), route, 40);
        buses.flush();
        routeService.delete(south.get().getId());
        configureCampuses(route);
        var third = new Bus("UDLA-03", route, route.getParadas().get(2).coordenada(), clock.instant());
        third.updateDetails("UDLA-03", route, 40);
        buses.save(third);
        for (int i = 0; i < retained.size(); i++) startAtCampus(retained.get(i), i);
        startAtCampus(third, 2);
    }

    private void configureCampuses(Ruta route) {
        stopService.update(route.getParadas().get(0).getId(), "Jipijapa", new Coordenada(-.1600, -78.4700));
        stopService.update(route.getParadas().get(1).getId(), "UDLA Granados", new Coordenada(-.1682, -78.4702));
        stopService.update(route.getParadas().get(2).getId(), "UDLA Park", new Coordenada(-.1450, -78.4590));
        route.updateDetails("IC-01", "Jipijapa · UDLA Granados · UDLA Park");
    }

    private void updatePreviousDemo() {
        var existingRoutes = routes.findAll();
        var existingBuses = buses.findAll();
        if (existingRoutes.size() != 2 || existingBuses.size() != 4) return;
        var north = existingRoutes.stream().filter(r -> r.getCodigo().equals("IC-01") && r.getNombre().equals("Conexión Norte")).findFirst();
        var south = existingRoutes.stream().filter(r -> r.getCodigo().equals("IC-02") && r.getNombre().equals("Circuito Intercampus")).findFirst();
        if (north.isEmpty() || south.isEmpty()) return;
        var route = north.get();
        if (!route.getParadas().stream().map(Parada::getNombre).toList().equals(List.of(
                "Campus Granados · Demo", "Punto de encuentro · Demo", "Campus Queri · Demo", "Campus UDLAPark · Demo"))) return;
        if (!south.get().getParadas().stream().map(Parada::getNombre).toList().equals(List.of(
                "Campus Granados · Demo", "Estación de conexión · Demo", "Punto Sur · Demo"))) return;
        if (!existingBuses.stream().map(Bus::getPlaca).collect(java.util.stream.Collectors.toSet())
                .equals(Set.of("UDLA-01", "UDLA-02", "UDLA-03", "UDLA-04"))) return;
        var obsolete = existingBuses.stream().filter(b -> b.getPlaca().equals("UDLA-04")).findFirst().orElseThrow();
        busService.delete(obsolete.getId());
        var retained = existingBuses.stream().filter(b -> !b.getPlaca().equals("UDLA-04"))
                .sorted(Comparator.comparing(Bus::getPlaca)).toList();
        for (Bus bus : retained) bus.updateDetails(bus.getPlaca(), route, bus.getCapacity());
        buses.flush();
        routeService.delete(south.get().getId());
        stopService.delete(route.getParadas().get(3).getId());
        configureCampuses(route);
        for (int i = 0; i < retained.size(); i++) startAtCampus(retained.get(i), i);
    }
}
