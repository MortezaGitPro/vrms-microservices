package com.champsoft.vrms.registration.application.service;

import com.champsoft.vrms.registration.application.exception.*;
import com.champsoft.vrms.registration.application.port.out.*;
import com.champsoft.vrms.registration.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RegistrationOrchestrator {

    private final VehicleEligibilityPort vehiclePort;
    private final OwnerEligibilityPort ownerPort;
    private final AgentEligibilityPort agentPort;
    private final RegistrationRepositoryPort repo;

    public RegistrationOrchestrator(
            VehicleEligibilityPort vehiclePort,
            OwnerEligibilityPort ownerPort,
            AgentEligibilityPort agentPort,
            RegistrationRepositoryPort repo
    ) {
        this.vehiclePort = vehiclePort;
        this.ownerPort = ownerPort;
        this.agentPort = agentPort;
        this.repo = repo;
    }

    @Transactional
    public Registration register(String vehicleId, String ownerId, String agentId, String plate, LocalDate expiry) {
        var plateVo = new PlateNumber(plate);

        if (repo.existsByPlate(plateVo.value())) {
            throw new PlateAlreadyTakenException("Plate already taken: " + plateVo.value());
        }

        if (!vehiclePort.isEligible(vehicleId)) {
            throw new CrossContextValidationException("Vehicle is not eligible (must be ACTIVE)");
        }
        if (!ownerPort.isEligible(ownerId)) {
            throw new CrossContextValidationException("Owner is not eligible");
        }
        if (!agentPort.isEligible(agentId)) {
            throw new CrossContextValidationException("Agent is not eligible");
        }

        var reg = new Registration(
                RegistrationId.newId(),
                new VehicleRef(vehicleId),
                new OwnerRef(ownerId),
                new AgentRef(agentId),
                plateVo,
                new ExpiryDate(expiry)
        );

        return repo.save(reg);
    }
}
