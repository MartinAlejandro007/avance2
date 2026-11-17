package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.dto.ProfileDto;
import ec.intercampus.intercampus.domain.VisualTheme;

public interface FavoriteService {
    ProfileDto profile(Long userId);
    void favoriteRoute(Long userId, Long routeId, boolean favorite);
    void favoriteStop(Long userId, Long stopId, boolean favorite);
    void changeTheme(Long userId, VisualTheme theme);
    void removeRouteReferences(Long routeId);
    void removeStopReferences(Long stopId);
}
