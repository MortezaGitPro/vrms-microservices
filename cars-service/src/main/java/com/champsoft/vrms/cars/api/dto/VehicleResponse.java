package com.champsoft.vrms.cars.api.dto;

public record VehicleResponse(
        String id,
        String vin,
        String make,
        String model,
        int year,
        String status) {}
