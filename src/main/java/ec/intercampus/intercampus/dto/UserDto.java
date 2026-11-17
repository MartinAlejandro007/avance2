package ec.intercampus.intercampus.dto;

import ec.intercampus.intercampus.domain.*;

public record UserDto(Long id, String name, String email, UserType type) {}
