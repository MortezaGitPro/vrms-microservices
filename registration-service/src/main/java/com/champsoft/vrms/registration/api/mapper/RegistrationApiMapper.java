package com.champsoft.vrms.registration.api.mapper;

import com.champsoft.vrms.registration.api.dto.RegistrationResponse;
import com.champsoft.vrms.registration.domain.model.Registration;

public class RegistrationApiMapper {
    public static RegistrationResponse toResponse(Registration r) {
        return new RegistrationResponse(
                r.id().value(),
                r.vehicleIdValue(),
                r.ownerIdValue(),
                r.agentIdValue(),
                r.plateValue(),
                r.expiryValue(),
                r.status().name()
        );
    }
}
