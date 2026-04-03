package com.champsoft.vrms.cars.infrastructure.persistence;

import com.champsoft.vrms.cars.application.port.out.VehicleRepositoryPort;
import com.champsoft.vrms.cars.domain.model.Vehicle;
import com.champsoft.vrms.cars.domain.model.VehicleId;
import com.champsoft.vrms.cars.domain.model.VehicleSpecs;
import com.champsoft.vrms.cars.domain.model.Vin;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaVehicleRepositoryAdapter implements VehicleRepositoryPort {

    private final SpringDataVehicleRepository jpa;

    public JpaVehicleRepositoryAdapter(SpringDataVehicleRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        var e = toEntity(vehicle);
        jpa.save(e);
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(VehicleId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Vehicle> findByVin(Vin vin) {
        return jpa.findByVin(vin.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByVin(Vin vin) {
        return jpa.existsByVin(vin.value());
    }

    @Override
    public List<Vehicle> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(VehicleId id) {
        jpa.deleteById(id.value());
    }

    private VehicleJpaEntity toEntity(Vehicle v) {
        var e = new VehicleJpaEntity();
        e.id = v.id().value();
        e.vin = v.vin().value();
        e.specs = new VehicleSpecsEmbeddable(
                v.specs().make(),
                v.specs().model(),
                v.specs().year()
        );
        e.status = v.status().name();
        return e;
    }

    private Vehicle toDomain(VehicleJpaEntity e) {
        var vehicle = new Vehicle(
                VehicleId.of(e.id),
                new Vin(e.vin),
                new VehicleSpecs(
                        e.specs.make,
                        e.specs.model,
                        e.specs.year
                )
        );

        if ("ACTIVE".equalsIgnoreCase(e.status)) {
            vehicle.activate();
        }

        return vehicle;
    }
}
