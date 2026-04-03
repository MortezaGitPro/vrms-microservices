package com.champsoft.vrms.registration.application.port.out;

import com.champsoft.vrms.registration.domain.model.*;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepositoryPort {
    Registration save(Registration reg);
    Optional<Registration> findById(RegistrationId id);
    Optional<Registration> findByPlate(String plate);
    boolean existsByPlate(String plate);
    List<Registration> findAll();
    void deleteById(RegistrationId id);
}
