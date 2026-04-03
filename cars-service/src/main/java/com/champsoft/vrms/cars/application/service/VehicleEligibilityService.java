package com.champsoft.vrms.cars.application.service;

import com.champsoft.vrms.cars.application.exception.VehicleNotFoundException;
import com.champsoft.vrms.cars.application.port.out.VehicleRepositoryPort;
import com.champsoft.vrms.cars.domain.model.VehicleId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleEligibilityService {

    private final VehicleRepositoryPort repo;

    public VehicleEligibilityService(VehicleRepositoryPort repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public boolean isEligible(String vehicleId) {
        return repo.findById(VehicleId.of(vehicleId))
                .map(v -> v.isEligibleForRegistration())
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + vehicleId));
    }
}
