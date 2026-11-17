package ec.intercampus.intercampus.alert;

import ec.intercampus.intercampus.dto.AlertDto;
import java.util.List;

public interface AlertService {
    void evaluate();
    List<AlertDto> findAll();
}
