package com.champsoft.vrms.cars.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateVehicleRequest(
        @NotBlank String vin,
        @NotBlank String make,
        @NotBlank String model,
        @Min(1980) @Max(2050) int year) {}
