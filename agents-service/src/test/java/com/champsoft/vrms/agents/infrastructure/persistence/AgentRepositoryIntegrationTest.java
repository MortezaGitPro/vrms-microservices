package com.champsoft.vrms.agents.infrastructure.persistence;

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

// Use testing profile → ensures we use H2 instead of PostgreSQL
@ActiveProfiles("testing")
class AgentRepositoryIntegrationTest {

    // Real repository (NOT mocked)
    // This is an integration test → we verify real database behavior
    @Autowired
    private SpringDataAgentRepository repository;

    @Test
    @DisplayName("Should save an agent successfully")
    void shouldSaveAgentSuccessfully() {

        // ------------------- Arrange -------------------
        // Create JPA entity (persistence model, not domain model)
        AgentJpaEntity agent = new AgentJpaEntity();
        agent.id = "agent-1";
        agent.name = "Alice Brown";

        // Role and status stored as Strings in DB
        agent.role = "CLERK";
        agent.status = "ACTIVE";

        // ------------------- Act -------------------
        // Save to database (H2)
        AgentJpaEntity saved = repository.save(agent);

        // ------------------- Assert -------------------
        // Verify persistence worked correctly
        assertThat(saved).isNotNull();
        assertThat(saved.id).isEqualTo("agent-1");
        assertThat(saved.name).isEqualTo("Alice Brown");
        assertThat(saved.role).isEqualTo("CLERK");
        assertThat(saved.status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should find an agent by id")
    void shouldFindAgentById() {

        // ------------------- Arrange -------------------
        AgentJpaEntity agent = new AgentJpaEntity();
        agent.id = "agent-2";
        agent.name = "Bob Martin";
        agent.role = "SUPERVISOR";
        agent.status = "ACTIVE";

        // Must save before querying
        repository.save(agent);

        // ------------------- Act -------------------
        Optional<AgentJpaEntity> found = repository.findById("agent-2");

        // ------------------- Assert -------------------
        // Verify record exists and values are correct
        assertThat(found).isPresent();
        assertThat(found.get().id).isEqualTo("agent-2");
        assertThat(found.get().name).isEqualTo("Bob Martin");
        assertThat(found.get().role).isEqualTo("SUPERVISOR");
        assertThat(found.get().status).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should return true when agent name exists ignoring case")
    void shouldReturnTrueWhenAgentNameExistsIgnoringCase() {

        // ------------------- Arrange -------------------
        AgentJpaEntity agent = new AgentJpaEntity();
        agent.id = "agent-3";
        agent.name = "Charlie Lee";
        agent.role = "CLERK";
        agent.status = "ACTIVE";

        repository.save(agent);

        // ------------------- Act -------------------
        // Case-insensitive query
        boolean exists = repository.existsByNameIgnoreCase("charlie lee");

        // ------------------- Assert -------------------
        // Name exists (ignoring case) → should return true
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when agent name does not exist")
    void shouldReturnFalseWhenAgentNameDoesNotExist() {

        // ------------------- Act -------------------
        boolean exists = repository.existsByNameIgnoreCase("NOT-EXISTING");

        // ------------------- Assert -------------------
        // Name not in DB → should return false
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should delete an agent successfully")
    void shouldDeleteAgentSuccessfully() {

        // ------------------- Arrange -------------------
        AgentJpaEntity agent = new AgentJpaEntity();
        agent.id = "agent-4";
        agent.name = "Delete Agent";
        agent.role = "CLERK";
        agent.status = "ACTIVE";

        repository.save(agent);

        // ------------------- Act -------------------
        // Delete from database
        repository.deleteById("agent-4");

        // Try to find again
        Optional<AgentJpaEntity> found = repository.findById("agent-4");

        // ------------------- Assert -------------------
        // Entity should no longer exist
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when agent id is not found")
    void shouldReturnEmptyWhenAgentIdIsNotFound() {

        // ------------------- Act -------------------
        Optional<AgentJpaEntity> found = repository.findById("UNKNOWN-ID");

        // ------------------- Assert -------------------
        // No record → empty result
        assertThat(found).isEmpty();
    }
}