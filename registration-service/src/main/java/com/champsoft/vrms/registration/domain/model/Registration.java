package com.champsoft.vrms.registration.domain.model;

import com.champsoft.vrms.registration.domain.exception.ExpiryDateMustBeFutureException;

public class Registration {
    private final RegistrationId id;
    private final VehicleRef vehicleId;
    private final OwnerRef ownerId;
    private final AgentRef agentId;

    private PlateNumber plate;
    private ExpiryDate expiry;
    private RegistrationStatus status;

    public Registration(RegistrationId id, VehicleRef vehicleId, OwnerRef ownerId, AgentRef agentId,
                        PlateNumber plate, ExpiryDate expiry) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.ownerId = ownerId;
        this.agentId = agentId;
        this.plate = plate;
        this.expiry = expiry;
        this.status = RegistrationStatus.ACTIVE;
    }

    public RegistrationId id() { return id; }
    public VehicleRef vehicleId() { return vehicleId; }
    public OwnerRef ownerId() { return ownerId; }
    public AgentRef agentId() { return agentId; }
    public PlateNumber plate() { return plate; }
    public ExpiryDate expiry() { return expiry; }
    public RegistrationStatus status() { return status; }

    // convenience accessors for API layer to avoid nested record accessor issues
    public String vehicleIdValue() { return vehicleId.value(); }
    public String ownerIdValue() { return ownerId.value(); }
    public String agentIdValue() { return agentId.value(); }
    public String plateValue() { return plate.value(); }
    public java.time.LocalDate expiryValue() { return expiry.value(); }

    public void renew(ExpiryDate newExpiry) {
        if (status != RegistrationStatus.ACTIVE) {
            throw new RuntimeException("Registration not ACTIVE");
        }
        if (!newExpiry.isFuture()) {
            throw new ExpiryDateMustBeFutureException("Expiry must be in the future");
        }
        this.expiry = newExpiry;
    }

    // Used for NEW registration creation
    public static Registration createNew(
            RegistrationId id,
            VehicleRef vehicleId,
            OwnerRef ownerId,
            AgentRef agentId,
            PlateNumber plate,
            ExpiryDate expiry
    ) {
        if (!expiry.isFuture()) {
            throw new ExpiryDateMustBeFutureException("Expiry must be in the future");
        }

        return new Registration(id, vehicleId, ownerId, agentId, plate, expiry);
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
    }
}
