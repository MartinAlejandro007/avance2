package ec.intercampus.intercampus.web;

import ec.intercampus.intercampus.domain.Coordenada;
import ec.intercampus.intercampus.service.BusService;
import ec.intercampus.intercampus.service.RouteService;
import ec.intercampus.intercampus.service.StopService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"demo.seed-enabled=false", "tracker.enabled=false"})
@AutoConfigureMockMvc
@Transactional
class StaffPermissionsTest {
    @Autowired MockMvc mvc;
    @Autowired RouteService routes;
    @Autowired StopService stops;
    @Autowired BusService buses;

    @Test
    void permiteConsultasPublicasSinMostrarGestionDeBuses() throws Exception {
        for (String path : new String[]{"/", "/routes", "/fleet", "/tracking", "/reports", "/login"})
            mvc.perform(get(path)).andExpect(status().isOk());
        mvc.perform(get("/fleet")).andExpect(content().string(not(containsString("Guardar unidad"))))
                .andExpect(content().string(containsString("Acceso de personal")));
        mvc.perform(get("/arrivals")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(post("/fleet").with(csrf())).andExpect(status().is3xxRedirection());
        assertTrue(buses.findAll().isEmpty());
    }

    @Test
    void administradorGestionaFlotaPeroNoConfirmaLlegadas() throws Exception {
        var route = routes.create("PERM-A", "Ruta");
        mvc.perform(get("/fleet").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Guardar unidad")));
        mvc.perform(post("/fleet").with(user("admin").roles("ADMIN")).with(csrf())
                        .param("plate", "PERM-01").param("routeId", route.id().toString())
                        .param("capacity", "40").param("latitude", "0").param("longitude", "0"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/fleet"));
        assertEquals(1, buses.findAll().size());
        mvc.perform(get("/arrivals").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(post("/arrivals/1").with(user("admin").roles("ADMIN")).with(csrf()).param("stopId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conductorConfirmaLlegadaPeroNoPuedeModificarFlota() throws Exception {
        var route = routes.create("PERM-C", "Ruta del conductor");
        var first = stops.create(route.id(), "Jipijapa", new Coordenada(0, 0));
        stops.create(route.id(), "Granados", new Coordenada(0, .01));
        var bus = buses.create("PERM-02", route.id(), 40, first.coordinate());
        mvc.perform(get("/arrivals").with(user("conductor").roles("DRIVER"))).andExpect(status().isOk());
        mvc.perform(post("/arrivals/" + bus.id() + "/control").with(user("conductor").roles("DRIVER")).with(csrf())
                        .param("manual", "true")).andExpect(status().is3xxRedirection());
        mvc.perform(post("/arrivals/" + bus.id()).with(user("conductor").roles("DRIVER")).with(csrf())
                        .param("stopId", first.id().toString())).andExpect(status().is3xxRedirection());
        assertTrue(buses.findById(bus.id()).manualControl());
        for (String path : new String[]{"/fleet", "/fleet/" + bus.id(), "/fleet/" + bus.id() + "/delete", "/fleet/" + bus.id() + "/status"})
            mvc.perform(post(path).with(user("conductor").roles("DRIVER")).with(csrf()))
                    .andExpect(status().isForbidden());
        assertEquals(1, buses.findAll().size());
    }

    @Test
    void exigeTokenCsrfInclusoParaAdministrador() throws Exception {
        mvc.perform(post("/fleet").with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
        assertTrue(buses.findAll().isEmpty());
    }

    @Test
    void autenticaCredencialesConfiguradasYRechazaLasIncorrectas() throws Exception {
        mvc.perform(formLogin().user("admin").password("Admin-test-2026"))
                .andExpect(authenticated().withRoles("ADMIN")).andExpect(redirectedUrl("/staff"));
        mvc.perform(formLogin().user("conductor").password("Driver-test-2026"))
                .andExpect(authenticated().withRoles("DRIVER")).andExpect(redirectedUrl("/staff"));
        mvc.perform(formLogin().user("admin").password("incorrecta"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void enviaCadaRolASuPanelYPermiteCerrarSesion() throws Exception {
        mvc.perform(get("/staff").with(user("admin").roles("ADMIN"))).andExpect(redirectedUrl("/fleet"));
        mvc.perform(get("/staff").with(user("conductor").roles("DRIVER"))).andExpect(redirectedUrl("/arrivals"));
        mvc.perform(post("/logout").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?logout"));
    }
}
