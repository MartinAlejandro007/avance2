package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.*;

public interface PositionService {
    Posicion current(Bus bus);
    void record(Bus bus, Posicion position);
    void deleteHistory(Long busId);
}
