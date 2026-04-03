package com.champsoft.vrms.owners.infrastructure.persistence;

import com.champsoft.vrms.owners.application.port.out.OwnerRepositoryPort;
import com.champsoft.vrms.owners.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaOwnerRepositoryAdapter implements OwnerRepositoryPort {

    private final SpringDataOwnerRepository jpa;

    public JpaOwnerRepositoryAdapter(SpringDataOwnerRepository jpa) { this.jpa = jpa; }

    @Override
    public Owner save(Owner owner) {
        jpa.save(toEntity(owner));
        return owner;
    }

    @Override
    public Optional<Owner> findById(OwnerId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByFullName(String fullName) {
        return jpa.existsByFullNameIgnoreCase(fullName);
    }

    @Override
    public List<Owner> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(OwnerId id) {
        jpa.deleteById(id.value());
    }

    private OwnerJpaEntity toEntity(Owner o) {
        var e = new OwnerJpaEntity();
        e.id = o.id().value();
        e.fullName = o.fullName().value();
        e.address = o.address().value();
        e.status = o.status().name();
        return e;
    }

    private Owner toDomain(OwnerJpaEntity e) {
        var owner = new Owner(
                OwnerId.of(e.id),
                new FullName(e.fullName),
                new Address(e.address)
        );
        if ("ACTIVE".equalsIgnoreCase(e.status)) owner.activate();
        if ("SUSPENDED".equalsIgnoreCase(e.status)) owner.suspend();
        return owner;
    }
}
