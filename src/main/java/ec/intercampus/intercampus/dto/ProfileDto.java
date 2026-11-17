package ec.intercampus.intercampus.dto;

import ec.intercampus.intercampus.domain.VisualTheme;
import java.util.List;

public record ProfileDto(UserDto user, List<RouteDto> favoriteRoutes, List<StopDto> favoriteStops, VisualTheme theme) {
    public ProfileDto { favoriteRoutes = List.copyOf(favoriteRoutes); favoriteStops = List.copyOf(favoriteStops); }
}
