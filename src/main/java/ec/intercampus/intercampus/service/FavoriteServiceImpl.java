package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;
import ec.intercampus.intercampus.dto.ProfileDto;
import ec.intercampus.intercampus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static ec.intercampus.intercampus.service.ServiceValidation.*;

@Service
@Transactional(readOnly = true)
public class FavoriteServiceImpl implements FavoriteService {
    private final UserProfileRepository users;
    private final RutaRepository routes;
    private final ParadaRepository stops;
    public FavoriteServiceImpl(UserProfileRepository users, RutaRepository routes, ParadaRepository stops) {
        this.users = users; this.routes = routes; this.stops = stops;
    }
    private UserProfile require(Long id) { return users.findById(id(id)).orElseThrow(() -> missing("Usuario")); }
    @Override
    public ProfileDto profile(Long userId) {
        var user = require(userId);
        return new ProfileDto(DtoMapper.user(user), user.getFavoriteRoutes().stream().map(DtoMapper::route).sorted(java.util.Comparator.comparing(r -> r.code())).toList(),
                user.getFavoriteStops().stream().map(DtoMapper::stop).sorted(java.util.Comparator.comparing(s -> s.name())).toList(), user.getTheme());
    }
    @Override @Transactional
    public void favoriteRoute(Long userId, Long routeId, boolean favorite) {
        var user = require(userId);
        user.favoriteRoute(routes.findById(id(routeId)).orElseThrow(() -> missing("Ruta")), favorite);
    }
    @Override @Transactional
    public void favoriteStop(Long userId, Long stopId, boolean favorite) {
        var user = require(userId);
        user.favoriteStop(stops.findById(id(stopId)).orElseThrow(() -> missing("Parada")), favorite);
    }
    @Override @Transactional
    public void changeTheme(Long userId, VisualTheme theme) {
        if (theme == null) throw new DomainException(DomainException.Code.INVALID_INPUT, "Selecciona una preferencia visual");
        require(userId).changeTheme(theme);
    }
    @Override @Transactional
    public void removeRouteReferences(Long routeId) {
        for (var user : users.findByFavoriteRoutesId(routeId)) {
            user.getFavoriteRoutes().stream().filter(r -> r.getId().equals(routeId)).findFirst().ifPresent(r -> user.favoriteRoute(r, false));
        }
    }
    @Override @Transactional
    public void removeStopReferences(Long stopId) {
        for (var user : users.findByFavoriteStopsId(stopId)) {
            user.getFavoriteStops().stream().filter(s -> s.getId().equals(stopId)).findFirst().ifPresent(s -> user.favoriteStop(s, false));
        }
    }
}
