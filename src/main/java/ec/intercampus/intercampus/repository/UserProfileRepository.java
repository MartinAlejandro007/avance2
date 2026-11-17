package ec.intercampus.intercampus.repository;

import ec.intercampus.intercampus.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByEmail(String value);
    boolean existsByEmailAndIdNot(String value, Long id);
    java.util.List<UserProfile> findByFavoriteRoutesId(Long routeId);
    java.util.List<UserProfile> findByFavoriteStopsId(Long stopId);
}

