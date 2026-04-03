package com.champsoft.vrms.registration.api.dto;

import java.time.LocalDate;

public record RegistrationResponse(
        String id,
        String vehicleId,
        String ownerId,
        String agentId,
        String plate,
        LocalDate expiry,
        String status) {}
