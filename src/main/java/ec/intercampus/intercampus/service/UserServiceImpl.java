package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.*;
import ec.intercampus.intercampus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static ec.intercampus.intercampus.service.ServiceValidation.*;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserProfileRepository repository;

    public UserServiceImpl(UserProfileRepository repository) {
        this.repository = repository;
    }
    @Override
    public List<UserDto> findAll() { return repository.findAll().stream().map(DtoMapper::user).toList(); }
    @Override
    public UserDto findById(Long id) { return DtoMapper.user(require(id)); }
    private UserProfile require(Long id) {
        return repository.findById(id(id)).orElseThrow(() -> missing("Usuario"));
    }
    @Override
    @Transactional
    public UserDto create(String name, String email, UserType type) {
        name = text(name, "El nombre");
        email = email(email);
        userType(type);
        unique(repository.existsByEmail(email));
        UserProfile entity = new UserProfile(name, email, type);
        return DtoMapper.user(repository.save(entity));
    }
    @Override
    @Transactional
    public UserDto update(Long id, String name, String email, UserType type) {
        UserProfile entity = require(id);
        name = text(name, "El nombre");
        email = email(email);
        userType(type);
        unique(repository.existsByEmailAndIdNot(email, id));
        entity.updateDetails(name, email, type);
        return DtoMapper.user(entity);
    }
    @Override
    @Transactional
    public void delete(Long id) {
        UserProfile entity = require(id);
        
        repository.delete(entity);
    }
}
