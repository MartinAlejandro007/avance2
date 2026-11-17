package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.DomainException;
import static ec.intercampus.intercampus.domain.DomainException.Code.*;

final class ServiceValidation {
    private ServiceValidation() { }
    static String text(String value, String field) {
        if (value == null || value.isBlank()) throw new DomainException(INVALID_INPUT, field + " es obligatorio");
        return value.trim();
    }
    static String email(String value) {
        String email = text(value, "El correo").toLowerCase(java.util.Locale.ROOT);
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new DomainException(INVALID_INPUT, "El correo no es válido");
        return email;
    }
    static void userType(ec.intercampus.intercampus.domain.UserType type) {
        if (type == null) throw new DomainException(INVALID_INPUT, "El tipo de usuario es obligatorio");
    }
    static void capacity(int value) {
        if (value <= 0) throw new DomainException(INVALID_INPUT, "La capacidad debe ser positiva");
    }
    static void coordinate(ec.intercampus.intercampus.domain.Coordenada value) {
        if (value == null) throw new DomainException(INVALID_INPUT, "La coordenada es obligatoria");
    }
    static Long id(Long value) {
        if (value == null || value <= 0) throw new DomainException(INVALID_INPUT, "El identificador debe ser positivo");
        return value;
    }
    static DomainException missing(String name) {
        return new DomainException(NOT_FOUND, name + " no encontrado");
    }
    static void unique(boolean duplicate) {
        if (duplicate) throw new DomainException(DUPLICATE, "Ya existe un registro con ese identificador único");
    }
    static void available(boolean inUse) {
        if (inUse) throw new DomainException(IN_USE, "El registro tiene unidades asignadas");
    }
}
