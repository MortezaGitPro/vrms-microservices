package com.champsoft.vrms.registration.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "registrations")
public class RegistrationJpaEntity {
    @Id
    public String id;

    @Column(name = "vehicle_id", nullable = false)
    public String vehicleId;

    @Column(name = "owner_id", nullable = false)
    public String ownerId;

    @Column(name = "agent_id", nullable = false)
    public String agentId;

    @Column(nullable = false, unique = true)
    public String plate;

    @Column(nullable = false)
    public LocalDate expiry;

    @Column(nullable = false)
    public String status;
}
