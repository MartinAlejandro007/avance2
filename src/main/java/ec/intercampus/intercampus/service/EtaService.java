package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.dto.EtaDto;

public interface EtaService {
    EtaDto calculate(Long busId, Long stopId);
}
