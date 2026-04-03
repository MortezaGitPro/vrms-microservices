package com.champsoft.vrms.owners.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "owners")
public class OwnerJpaEntity {
    @Id
    public String id;

    @Column(name = "full_name", nullable = false)
    public String fullName;

    @Column(name = "address")
    public String address;

    @Column(nullable = false)
    public String status;
}
