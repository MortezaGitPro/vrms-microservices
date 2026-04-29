package com.champsoft.vrms.owners.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

// @DataJpaTest → loads only JPA layer (entities + repositories)
// Starts an in-memory database (H2) automatically
@DataJpaTest

// Use "testing" profile → ensures H2 is used instead of PostgreSQL
@ActiveProfiles("testing")
class OwnerRepositoryIntegrationTest {

    // Real repository (NOT mocked)
    // This is an integration test → we test real database behavior
    @Autowired
    private SpringDataOwnerRepository repository;

    @Test
    @DisplayName("Should save an owner successfully")
    void shouldSaveOwnerSuccessfully() {

        // ------------------- Arrange -------------------
        // Create JPA entity (this is persistence model, not domain model)
        OwnerJpaEntity owner = new OwnerJpaEntity();
        owner.id = "owner-1";
        owner.fullName = "John Smith";
        owner.address = "Montreal";

        // Stored as String in DB
        owner.status = "ACTIVE";

        // ------------------- Act -------------------
        // Save to database (H2)
        OwnerJpaEntity saved = repository.save(owner);

        // ------------------- Assert -------------------
        // Verify entity persisted correctly
        assertThat(saved).isNotNull();
        assertThat(saved.id).isEqualTo("owner-1");
        assertThat(saved.fullName).isEqualTo("John Smith");
        assertThat(saved.address).isEqualTo("Montreal");
        assertThat(saved.status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should find an owner by id")
    void shouldFindOwnerById() {

        // ------------------- Arrange -------------------
        OwnerJpaEntity owner = new OwnerJpaEntity();
        owner.id = "owner-2";
        owner.fullName = "Alice Brown";
        owner.address = "Laval";
        owner.status = "ACTIVE";

        // Must save before querying
        repository.save(owner);

        // ------------------- Act -------------------
        Optional<OwnerJpaEntity> found = repository.findById("owner-2");

        // ------------------- Assert -------------------
        // Verify record exists and values are correct
        assertThat(found).isPresent();
        assertThat(found.get().id).isEqualTo("owner-2");
        assertThat(found.get().fullName).isEqualTo("Alice Brown");
        assertThat(found.get().address).isEqualTo("Laval");
        assertThat(found.get().status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should return true when owner full name exists ignoring case")
    void shouldReturnTrueWhenOwnerFullNameExistsIgnoringCase() {

        // ------------------- Arrange -------------------
        OwnerJpaEntity owner = new OwnerJpaEntity();
        owner.id = "owner-3";
        owner.fullName = "Bob Martin";
        owner.address = "Longueuil";
        owner.status = "ACTIVE";

        repository.save(owner);

        // ------------------- Act -------------------
        // Case-insensitive query
        boolean exists = repository.existsByFullNameIgnoreCase("bob martin");

        // ------------------- Assert -------------------
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when owner full name does not exist")
    void shouldReturnFalseWhenOwnerFullNameDoesNotExist() {

        // ------------------- Act -------------------
        boolean exists = repository.existsByFullNameIgnoreCase("NOT-EXISTING");

        // ------------------- Assert -------------------
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should delete an owner successfully")
    void shouldDeleteOwnerSuccessfully() {

        // ------------------- Arrange -------------------
        OwnerJpaEntity owner = new OwnerJpaEntity();
        owner.id = "owner-4";
        owner.fullName = "Delete Me";
        owner.address = "Brossard";
        owner.status = "ACTIVE";

        repository.save(owner);

        // ------------------- Act -------------------
        // Delete from database
        repository.deleteById("owner-4");

        // Try to find again
        Optional<OwnerJpaEntity> found = repository.findById("owner-4");

        // ------------------- Assert -------------------
        // Entity should no longer exist
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when owner id is not found")
    void shouldReturnEmptyWhenOwnerIdIsNotFound() {

        // ------------------- Act -------------------
        Optional<OwnerJpaEntity> found = repository.findById("UNKNOWN-ID");

        // ------------------- Assert -------------------
        // No record → empty result
        assertThat(found).isEmpty();
    }
}