package com.champsoft.vrms.cars.domain.model;

import com.champsoft.vrms.cars.domain.exception.VehicleAlreadyActiveException;

public class Vehicle {
    private final VehicleId id;
    private final Vin vin;
    private VehicleSpecs specs;
    private VehicleStatus status;

    public Vehicle(VehicleId id, Vin vin, VehicleSpecs specs) {
        this.id = id;
        this.vin = vin;
        this.specs = specs;
        this.status = VehicleStatus.INACTIVE;
    }

    public VehicleId id() { return id; }
    public Vin vin() { return vin; }
    public VehicleSpecs specs() { return specs; }
    public VehicleStatus status() { return status; }

    public void updateSpecs(VehicleSpecs newSpecs) {
        this.specs = newSpecs;
    }

    public void activate() {
        if (status == VehicleStatus.ACTIVE) {
            throw new VehicleAlreadyActiveException("Vehicle is already ACTIVE");
        }
        this.status = VehicleStatus.ACTIVE;
    }

    public boolean isEligibleForRegistration() {
        return status == VehicleStatus.ACTIVE;
    }
}
