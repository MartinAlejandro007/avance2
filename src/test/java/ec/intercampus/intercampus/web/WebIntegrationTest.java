package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"demo.seed-enabled=false", "tracker.enabled=false"})
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = {"ADMIN", "DRIVER"})
class WebIntegrationTest {
    private static MockHttpServletRequestBuilder securedPost(String path) {
        return post(path).with(csrf());
    }
    @Autowired MockMvc mvc;
    @Autowired RouteService routes;
    @Autowired StopService stops;
    @Autowired UserService users;
    @Autowired FavoriteService favorites;
    @Autowired ec.intercampus.intercampus.repository.BusRepository busRepository;
    @Autowired ec.intercampus.intercampus.simulation.PositionSimulator simulator;
    @Autowired PositionService positions;
    @Autowired BusService buses;
    @Autowired ec.intercampus.intercampus.repository.ArrivalRecordRepository arrivals;

    @Test
    void rendersPagesAndEditFormsWithRealTemplates() throws Exception {
        var route = routes.create("WEB-01", "Ruta de prueba");
        var stop = stops.create(route.id(), "Parada de prueba", new Coordenada(-.16, -78.47));
        var user = users.create("Prueba", "prueba@udla.edu.ec", UserType.STUDENT);
        var bus = buses.create("WEB-BUS-01", route.id(), 40, new Coordenada(-.16, -78.47));
        for (String path : new String[]{"/", "/routes", "/routes?edit=" + route.id(),
                "/routes/" + route.id(), "/routes/" + route.id() + "?editStop=" + stop.id(),
                "/fleet", "/arrivals", "/fleet?edit=" + bus.id(), "/alerts", "/profiles", "/profiles?edit=" + user.id(),
                "/profiles/" + user.id(), "/reports"}) {
            mvc.perform(get(path)).andExpect(status().isOk())
                    .andExpect(content().string(containsString("CampusUDLATracker")));
        }
        mvc.perform(get("/tracking").param("routeId", route.id().toString()).param("stopId", stop.id().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.routes[0].id").value(route.id()));
        mvc.perform(get("/css/app.css")).andExpect(status().isOk());
        mvc.perform(get("/js/app.js")).andExpect(status().isOk());
    }

    @Test
    void savesFavoritesAndThemeThroughForms() throws Exception {
        var route = routes.create("WEB-02", "Favoritos");
        var stop = stops.create(route.id(), "Parada favorita", new Coordenada(-.16, -78.47));
        var user = users.create("Favoritos", "favoritos@udla.edu.ec", UserType.STUDENT);
        String base = "/profiles/" + user.id();
        mvc.perform(securedPost(base + "/routes/" + route.id()).param("favorite", "true")).andExpect(status().is3xxRedirection());
        mvc.perform(securedPost(base + "/stops/" + stop.id()).param("favorite", "true")).andExpect(status().is3xxRedirection());
        mvc.perform(securedPost(base + "/theme").param("theme", "DARK")).andExpect(status().is3xxRedirection());
        assertEquals(1, favorites.profile(user.id()).favoriteRoutes().size());
        assertEquals(1, favorites.profile(user.id()).favoriteStops().size());
        assertEquals(VisualTheme.DARK, favorites.profile(user.id()).theme());
        mvc.perform(get(base)).andExpect(status().isOk()).andExpect(content().string(containsString("DARK")));
    }

    @Test
    void rendersReportWithPersistedArrivals() throws Exception {
        var route = routes.create("WEB-03", "Reporte de prueba");
        var expected = java.time.Instant.parse("2026-11-17T14:00:00Z");
        arrivals.save(new ArrivalRecord(route.id(), route.name(), "WEB-BUS", "Destino", expected, expected.plusSeconds(180)));
        mvc.perform(get("/reports").param("from", "2026-11-17").param("until", "2026-11-17")
                        .param("routeId", route.id().toString()))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Reporte de prueba")))
                .andExpect(content().string(containsString("Mañana")))
                .andExpect(content().string(containsString("<meter")));
    }

    @Test
    void registraLlegadaManualSinDuplicarlaConElSimulador() throws Exception {
        var route = routes.create("MAN-01", "Campus manuales");
        var first = stops.create(route.id(), "Jipijapa", new Coordenada(-.16, -78.47));
        var next = stops.create(route.id(), "UDLA Granados", new Coordenada(-.1682, -78.4702));
        var bus = buses.create("MAN-01", route.id(), 40, first.coordinate());
        String url = "/arrivals/" + bus.id();
        mvc.perform(securedPost(url + "/control").param("manual", "true")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/arrivals")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Llegué a Jipijapa")));
        mvc.perform(securedPost(url).param("stopId", first.id().toString())).andExpect(status().is3xxRedirection());
        simulator.advance();
        assertEquals(1, arrivals.count());
        var recorded = arrivals.findAll().get(0);
        assertEquals(ArrivalSource.DRIVER_CONFIRMED, recorded.getSource());
        assertEquals("Jipijapa", recorded.getStopName());
        assertEquals(first.coordinate(), positions.current(busRepository.findById(bus.id()).orElseThrow()).coordenada());
        assertTrue(buses.findById(bus.id()).manualControl());
        assertEquals(next.id(), buses.findById(bus.id()).journey().arrival().stopId());
        mvc.perform(get("/arrivals")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Llegué a UDLA Granados")))
                .andExpect(content().string(containsString("data-local-time")));
        mvc.perform(securedPost(url).param("stopId", first.id().toString())).andExpect(status().isBadRequest());
        assertEquals(1, arrivals.count());
    }

    @Test
    void validaModoParadaYEstadoYPermiteVolverALaSimulacion() throws Exception {
        var route = routes.create("MAN-02", "Validaciones manuales");
        var first = stops.create(route.id(), "Inicio", new Coordenada(0, 0));
        var next = stops.create(route.id(), "Destino", new Coordenada(0, .01));
        var bus = buses.create("MAN-02", route.id(), 40, first.coordinate());
        String url = "/arrivals/" + bus.id();
        mvc.perform(securedPost(url).param("stopId", first.id().toString())).andExpect(status().isBadRequest());
        mvc.perform(securedPost(url + "/control").param("manual", "true")).andExpect(status().is3xxRedirection());
        mvc.perform(securedPost(url).param("stopId", next.id().toString())).andExpect(status().isBadRequest());
        buses.changeStatus(bus.id(), OperationalStatus.MAINTENANCE);
        mvc.perform(securedPost(url).param("stopId", first.id().toString())).andExpect(status().isBadRequest());
        mvc.perform(securedPost(url + "/control").param("manual", "false")).andExpect(status().is3xxRedirection());
        buses.changeStatus(bus.id(), OperationalStatus.ACTIVE);
        simulator.advance();
        assertFalse(buses.findById(bus.id()).manualControl());
        assertEquals(1, arrivals.count());
        assertEquals(ArrivalSource.SIMULATED, arrivals.findAll().get(0).getSource());
    }

    @Test
    void rendersValidationAndMissingRecordErrors() throws Exception {
        mvc.perform(get("/routes/999999")).andExpect(status().isNotFound());
        mvc.perform(securedPost("/routes").param("code", "").param("name", ""))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/reports").param("from", "2026-11-17").param("until", "2026-11-01"))
                .andExpect(status().isBadRequest());
    }
}
