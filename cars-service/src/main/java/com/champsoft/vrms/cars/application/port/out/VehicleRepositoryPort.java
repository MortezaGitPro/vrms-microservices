package com.champsoft.vrms.cars.application.port.out;

import com.champsoft.vrms.cars.domain.model.Vehicle;
import com.champsoft.vrms.cars.domain.model.VehicleId;
import com.champsoft.vrms.cars.domain.model.Vin;

import java.util.List;
import java.util.Optional;

public interface VehicleRepositoryPort {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(VehicleId id);
    Optional<Vehicle> findByVin(Vin vin);
    boolean existsByVin(Vin vin);
    List<Vehicle> findAll();
    void deleteById(VehicleId id);
}
