package com.champsoft.vrms.registration.infrastructure.persistence;

import com.champsoft.vrms.registration.application.port.out.RegistrationRepositoryPort;
import com.champsoft.vrms.registration.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaRegistrationRepositoryAdapter implements RegistrationRepositoryPort {

    private final SpringDataRegistrationRepository jpa;
    public JpaRegistrationRepositoryAdapter(SpringDataRegistrationRepository jpa) { this.jpa = jpa; }

    @Override
    public Registration save(Registration reg) {
        jpa.save(toEntity(reg));
        return reg;
    }

    @Override
    public Optional<Registration> findById(RegistrationId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Registration> findByPlate(String plate) {
        return jpa.findByPlate(plate).map(this::toDomain);
    }

    @Override
    public boolean existsByPlate(String plate) {
        return jpa.existsByPlate(plate);
    }

    @Override
    public List<Registration> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(RegistrationId id) {
        jpa.deleteById(id.value());
    }

    private RegistrationJpaEntity toEntity(Registration reg) {
        var e = new RegistrationJpaEntity();
        e.id = reg.id().value();
        e.vehicleId = reg.vehicleId().value();
        e.ownerId = reg.ownerId().value();
        e.agentId = reg.agentId().value();
        e.plate = reg.plate().value();
        e.expiry = reg.expiry().value();
        e.status = reg.status().name();
        return e;
    }

    private Registration toDomain(RegistrationJpaEntity e) {
        var reg = new Registration(
                RegistrationId.of(e.id),
                new VehicleRef(e.vehicleId),
                new OwnerRef(e.ownerId),
                new AgentRef(e.agentId),
                new PlateNumber(e.plate),
                new ExpiryDate(e.expiry)
        );
        if ("CANCELLED".equalsIgnoreCase(e.status)) {
            reg.cancel();
        }
        return reg;
    }
}
