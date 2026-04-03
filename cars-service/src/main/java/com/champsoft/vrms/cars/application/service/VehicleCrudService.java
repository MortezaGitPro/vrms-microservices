package com.champsoft.vrms.cars.application.service;

import com.champsoft.vrms.cars.application.exception.DuplicateVinException;
import com.champsoft.vrms.cars.application.exception.VehicleNotFoundException;
import com.champsoft.vrms.cars.application.port.out.VehicleRepositoryPort;
import com.champsoft.vrms.cars.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleCrudService {

    private final VehicleRepositoryPort repo;

    public VehicleCrudService(VehicleRepositoryPort repo) {
        this.repo = repo;
    }

    @Transactional
    public Vehicle create(String vin, String make, String model, int year) {
        var v = new Vin(vin);
        if (repo.existsByVin(v)) throw new DuplicateVinException("VIN already exists: " + v.value());

        var vehicle = new Vehicle(VehicleId.newId(), v, new VehicleSpecs(make, model, year));
        return repo.save(vehicle);
    }

    @Transactional(readOnly = true)
    public Vehicle getById(String id) {
        return repo.findById(VehicleId.of(id))
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found: " + id));
    }

    @Transactional(readOnly = true)
    public Vehicle getByVin(String vin) {
        return repo.findByVin(new Vin(vin))
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found by VIN: " + vin));
    }

    @Transactional(readOnly = true)
    public List<Vehicle> list() {
        return repo.findAll();
    }

    @Transactional
    public Vehicle updateSpecs(String id, String make, String model, int year) {
        var vehicle = getById(id);
        vehicle.updateSpecs(new VehicleSpecs(make, model, year));
        return repo.save(vehicle);
    }

    @Transactional
    public Vehicle activate(String id) {
        var vehicle = getById(id);
        vehicle.activate();
        return repo.save(vehicle);
    }

    @Transactional
    public void delete(String id) {
        // ensure exists
        getById(id);
        repo.deleteById(VehicleId.of(id));
    }
}
