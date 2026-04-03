package com.champsoft.vrms.cars.domain.model;

import com.champsoft.vrms.cars.domain.exception.InvalidVehicleYearException;

public record VehicleSpecs(String make, String model, int year) {
    public VehicleSpecs {
        if (make == null || make.trim().isEmpty()) throw new IllegalArgumentException("make is required");
        if (model == null || model.trim().isEmpty()) throw new IllegalArgumentException("model is required");
        if (year < 1980 || year > 2050) throw new InvalidVehicleYearException("year must be between 1980 and 2050");
    }
}
