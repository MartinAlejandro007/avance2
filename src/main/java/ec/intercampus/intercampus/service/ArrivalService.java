package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.dto.ArrivalLogDto;
import java.util.List;

public interface ArrivalService {
    void changeControl(Long busId, boolean manual);
    void confirm(Long busId, Long stopId);
    List<ArrivalLogDto> recentConfirmations();
}
