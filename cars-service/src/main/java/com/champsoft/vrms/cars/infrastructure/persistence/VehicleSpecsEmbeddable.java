package com.champsoft.vrms.cars.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class VehicleSpecsEmbeddable {

    @Column(name = "make", nullable = false)
    public String make;

    @Column(name = "model", nullable = false)
    public String model;

    @Column(name = "vehicle_year", nullable = false)
    public int year;

    protected VehicleSpecsEmbeddable() {
    }

    public VehicleSpecsEmbeddable(String make, String model, int year) {
        this.make = make;
        this.model = model;
        this.year = year;
    }
}
