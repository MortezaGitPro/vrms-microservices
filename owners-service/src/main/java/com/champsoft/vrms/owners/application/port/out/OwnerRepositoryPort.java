package com.champsoft.vrms.owners.application.port.out;

import com.champsoft.vrms.owners.domain.model.Owner;
import com.champsoft.vrms.owners.domain.model.OwnerId;

import java.util.List;
import java.util.Optional;

public interface OwnerRepositoryPort {
    Owner save(Owner owner);
    Optional<Owner> findById(OwnerId id);
    boolean existsByFullName(String fullName);
    List<Owner> findAll();
    void deleteById(OwnerId id);
}
