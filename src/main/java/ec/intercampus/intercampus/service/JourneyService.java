package ec.intercampus.intercampus.service;

import ec.intercampus.intercampus.domain.Bus;
import ec.intercampus.intercampus.dto.JourneyDto;

public interface JourneyService {
    JourneyDto describe(Bus bus);
}
