package com.champsoft.vrms.owners.application.service;

import com.champsoft.vrms.owners.application.exception.OwnerNotFoundException;
import com.champsoft.vrms.owners.application.port.out.OwnerRepositoryPort;
import com.champsoft.vrms.owners.domain.model.OwnerId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerEligibilityService {

    private final OwnerRepositoryPort repo;
    public OwnerEligibilityService(OwnerRepositoryPort repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public boolean isEligible(String ownerId) {
        return repo.findById(OwnerId.of(ownerId))
                .map(o -> o.isEligibleForRegistration())
                .orElseThrow(() -> new OwnerNotFoundException("Owner not found: " + ownerId));
    }
}
