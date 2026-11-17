package ec.intercampus.intercampus;

import ec.intercampus.intercampus.domain.Coordenada;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:context-test", "spring.jpa.hibernate.ddl-auto=create-drop"})
class IntercampusApplicationTests {

    @Test
    void contextoSpringCargaCorrectamente() {
        // Si el contexto levanta, entidades, repositorios y datos iniciales estan bien cableados.
    }

    @Test
    void coordenadaRechazaValoresFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> new Coordenada(91, 0));
        assertEquals(-2.17, new Coordenada(-2.17, -79.92).latitud());
    }
}
