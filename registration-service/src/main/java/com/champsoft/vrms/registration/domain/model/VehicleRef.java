package com.champsoft.vrms.registration.domain.model;

public record VehicleRef(String value) {
    public VehicleRef {
        if (value == null) throw new IllegalArgumentException("vehicleId is required");
        value = value.trim();
        if (value.isEmpty()) throw new IllegalArgumentException("vehicleId is required");
    }
}
