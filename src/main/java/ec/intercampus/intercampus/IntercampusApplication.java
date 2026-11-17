package ec.intercampus.intercampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de Intercampus: localizador de rutas de buses universitarios.
 */
@SpringBootApplication
public class IntercampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntercampusApplication.class, args);
    }
}
