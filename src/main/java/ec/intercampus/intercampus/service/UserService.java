package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.dto.*;
import ec.intercampus.intercampus.domain.*;

public interface UserService {
    java.util.List<UserDto> findAll();
    UserDto findById(Long id);
    UserDto create(String name, String email, UserType type);
    UserDto update(Long id, String name, String email, UserType type);
    void delete(Long id);
}
