package ec.intercampus.intercampus;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.repository.*;
import ec.intercampus.intercampus.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:demo-test", "demo.seed-enabled=true", "tracker.enabled=false"})
@Transactional
class DemoConfigurationTest {
    @Autowired DatosIniciales demo;
    @Autowired RutaRepository routes;
    @Autowired BusRepository buses;
    @Autowired BusService busService;
    @Autowired RouteService routeService;
    @Autowired UserProfileRepository users;
    @Autowired FavoriteService favorites;

    @Test
    void iniciaTresCampusYTresBusesSinDuplicarlosAlReiniciar() {
        demo.run();
        assertDemo();
        assertTrue(busService.findAll().stream().allMatch(b -> b.journey().state().startsWith("Llegó a ")));
    }

    @Test
    void actualizaEscenarioAnteriorYConservaPerfilesYFavoritosValidos() {
        for (var bus : buses.findAll()) busService.delete(bus.getId());
        buses.flush();
        for (var route : routes.findAll()) routeService.delete(route.getId());
        routes.flush();
        var north = new Ruta("IC-01", "Conexión Norte");
        for (String name : List.of("Campus Granados · Demo", "Punto de encuentro · Demo", "Campus Queri · Demo", "Campus UDLAPark · Demo"))
            north.agregarParada(new Parada(name, new Coordenada(0, 0)));
        routes.save(north);
        var south = new Ruta("IC-02", "Circuito Intercampus");
        for (String name : List.of("Campus Granados · Demo", "Estación de conexión · Demo", "Punto Sur · Demo"))
            south.agregarParada(new Parada(name, new Coordenada(0, 0)));
        routes.save(south);
        for (int i = 1; i <= 4; i++) {
            var route = i <= 2 ? north : south;
            var bus = new Bus("UDLA-0" + i, route, new Coordenada(0, 0), Instant.EPOCH);
            bus.updateDetails(bus.getPlaca(), route, 40);
            buses.save(bus);
        }
        var user = users.findAll().get(0);
        favorites.favoriteRoute(user.getId(), north.getId(), true);
        favorites.favoriteStop(user.getId(), north.getParadas().get(0).getId(), true);
        favorites.favoriteRoute(user.getId(), south.getId(), true);
        favorites.favoriteStop(user.getId(), south.getParadas().get(0).getId(), true);
        demo.run();
        routes.flush();
        assertDemo();
        assertEquals(1, favorites.profile(user.getId()).favoriteRoutes().size());
        assertEquals("Jipijapa", favorites.profile(user.getId()).favoriteStops().get(0).name());
        demo.run();
        assertDemo();
    }

    @Test
    void respetaRecorridosPersonalizados() {
        var route = routes.findAll().get(0);
        route.updateDetails("PERSONAL", "Mi recorrido");
        demo.run();
        assertEquals("Mi recorrido", routes.findById(route.getId()).orElseThrow().getNombre());
    }

    private void assertDemo() {
        assertEquals(1, routes.count());
        assertEquals(List.of("Jipijapa", "UDLA Granados", "UDLA Park"),
                routes.findAll().get(0).getParadas().stream().map(Parada::getNombre).toList());
        assertEquals(3, buses.count());
        assertEquals(List.of("UDLA-01", "UDLA-02", "UDLA-03"),
                buses.findAll().stream().map(Bus::getPlaca).sorted().toList());
        assertTrue(buses.findAll().stream().allMatch(b -> b.getRuta().getId().equals(routes.findAll().get(0).getId())));
    }
}
