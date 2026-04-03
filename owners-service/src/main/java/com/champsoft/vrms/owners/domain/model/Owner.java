package com.champsoft.vrms.owners.domain.model;

public class Owner {
    private final OwnerId id;
    private FullName fullName;
    private Address address;
    private OwnerStatus status;

    public Owner(OwnerId id, FullName fullName, Address address) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.status = OwnerStatus.INACTIVE;
    }

    public OwnerId id() { return id; }
    public FullName fullName() { return fullName; }
    public Address address() { return address; }
    public OwnerStatus status() { return status; }

    public void update(FullName name, Address address) {
        this.fullName = name;
        this.address = address;
    }

    public void activate() {
        this.status = OwnerStatus.ACTIVE;
    }

    public void suspend() {
        this.status = OwnerStatus.SUSPENDED;
    }

    public boolean isEligibleForRegistration() {
        return status == OwnerStatus.ACTIVE;
    }
}
