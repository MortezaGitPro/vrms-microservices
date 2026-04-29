package com.champsoft.vrms.agents.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class AgentIdTest {

    @Test
    void shouldCreateAgentIdFromValue() {

        // ------------------- Act -------------------
        // Create an AgentId value object from a fixed string value.
        // This is useful when we already know the ID, for example in tests or when loading existing data.
        AgentId id = AgentId.of("agent-1");

        // ------------------- Assert -------------------
        // Verify that the AgentId stores the expected value.
        assertThat(id.value()).isEqualTo("agent-1");

        // toString() should also return the ID value.
        // This makes the object easier to print, log, or compare in simple messages.
        assertThat(id.toString()).isEqualTo("agent-1");
    }

    @Test
    void shouldCreateNewAgentId() {

        // ------------------- Act -------------------
        // Create a new AgentId automatically.
        // Usually this method generates a unique ID for a new agent.
        AgentId id = AgentId.newId();

        // ------------------- Assert -------------------
        // The generated ID object should not be null.
        assertThat(id).isNotNull();

        // The generated ID value should not be empty or blank.
        assertThat(id.value()).isNotBlank();
    }

    @Test
    void shouldCompareAgentIdsCorrectly() {

        // ------------------- Arrange -------------------
        // Create two AgentId objects with the same value.
        AgentId id1 = AgentId.of("agent-1");
        AgentId id2 = AgentId.of("agent-1");

        // Create another AgentId object with a different value.
        AgentId id3 = AgentId.of("agent-2");

        // ------------------- Assert -------------------
        // Two AgentId objects with the same value should be equal.
        assertThat(id1).isEqualTo(id2);

        // AgentId objects with different values should not be equal.
        assertThat(id1).isNotEqualTo(id3);

        // Equal objects should have the same hashCode.
        // This is important when objects are used in collections such as HashSet or HashMap.
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}