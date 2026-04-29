package com.champsoft.vrms.owners.application.service;

import com.champsoft.vrms.owners.application.exception.OwnerNotFoundException;
import com.champsoft.vrms.owners.application.port.out.OwnerRepositoryPort;
import com.champsoft.vrms.owners.domain.model.Address;
import com.champsoft.vrms.owners.domain.model.FullName;
import com.champsoft.vrms.owners.domain.model.Owner;
import com.champsoft.vrms.owners.domain.model.OwnerId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Enable Mockito → this is a pure service-layer test
// No Spring Boot, no database, no HTTP
@ExtendWith(MockitoExtension.class)
class OwnerEligibilityServiceTest {

    // Mocked repository → replaces real database
    // We control what data is returned
    @Mock
    private OwnerRepositoryPort repo;

    // Real service under test
    // Mockito injects the mocked repository into this service
    @InjectMocks
    private OwnerEligibilityService service;

    // Helper method → creates a valid Owner
    // Default state = INACTIVE (important for eligibility logic)
    private Owner sampleOwner() {
        return new Owner(
                OwnerId.of("owner-1"),
                new FullName("John Smith"),
                new Address("Montreal")
        );
    }

    @Test
    void shouldReturnTrueWhenOwnerIsActive() {

        // ------------------- Arrange -------------------
        // Create owner and activate it
        // Business rule: ACTIVE → eligible
        Owner owner = sampleOwner();
        owner.activate();

        // Mock repository → return this active owner
        when(repo.findById(OwnerId.of("owner-1")))
                .thenReturn(Optional.of(owner));

        // ------------------- Act -------------------
        boolean result = service.isEligible("owner-1");

        // ------------------- Assert -------------------
        // ACTIVE owner should be eligible
        assertThat(result).isTrue();

        // Verify repository lookup happened
        verify(repo).findById(OwnerId.of("owner-1"));
    }

    @Test
    void shouldReturnFalseWhenOwnerIsInactive() {

        // ------------------- Arrange -------------------
        // Create owner but DO NOT activate it
        // Default state = INACTIVE
        Owner owner = sampleOwner();

        // Mock repository → return inactive owner
        when(repo.findById(OwnerId.of("owner-1")))
                .thenReturn(Optional.of(owner));

        // ------------------- Act -------------------
        boolean result = service.isEligible("owner-1");

        // ------------------- Assert -------------------
        // INACTIVE owner should NOT be eligible
        assertThat(result).isFalse();

        // Verify repository call
        verify(repo).findById(OwnerId.of("owner-1"));
    }

    @Test
    void shouldReturnFalseWhenOwnerIsSuspended() {

        // ------------------- Arrange -------------------
        // Create owner and suspend it
        // Business rule: SUSPENDED → NOT eligible
        Owner owner = sampleOwner();
        owner.suspend();

        // Mock repository → return suspended owner
        when(repo.findById(OwnerId.of("owner-1")))
                .thenReturn(Optional.of(owner));

        // ------------------- Act -------------------
        boolean result = service.isEligible("owner-1");

        // ------------------- Assert -------------------
        // SUSPENDED owner should NOT be eligible
        assertThat(result).isFalse();

        // Verify repository lookup
        verify(repo).findById(OwnerId.of("owner-1"));
    }

    @Test
    void shouldThrowOwnerNotFoundExceptionWhenOwnerDoesNotExist() {

        // ------------------- Arrange -------------------
        // Repository returns empty → owner not found
        when(repo.findById(OwnerId.of("missing-owner")))
                .thenReturn(Optional.empty());

        // ------------------- Act + Assert -------------------
        // Service should throw exception when owner does not exist
        assertThrows(OwnerNotFoundException.class,
                () -> service.isEligible("missing-owner"));

        // Verify repository lookup was attempted
        verify(repo).findById(OwnerId.of("missing-owner"));
    }
}