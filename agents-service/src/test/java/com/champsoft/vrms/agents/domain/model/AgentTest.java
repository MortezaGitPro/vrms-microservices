package com.champsoft.vrms.agents.domain.model;

import com.champsoft.vrms.agents.domain.exception.InvalidAgentNameException;
import com.champsoft.vrms.agents.domain.exception.InvalidRoleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class AgentTest {

    @Test
    void shouldCreateAgentWithInactiveStatus() {

        // ------------------- Act -------------------
        // Create a new Agent domain object.
        // Business rule: a newly created agent should start as INACTIVE.
        Agent agent = new Agent(
                AgentId.of("agent-1"),
                "Alice Brown",
                Role.CLERK
        );

        // ------------------- Assert -------------------
        // Verify that the agent stores the expected ID.
        assertThat(agent.id().value()).isEqualTo("agent-1");

        // Verify that the agent stores the expected name.
        assertThat(agent.name()).isEqualTo("Alice Brown");

        // Verify that the agent stores the expected role.
        assertThat(agent.role()).isEqualTo(Role.CLERK);

        // Verify the default business state.
        assertThat(agent.status()).isEqualTo(AgentStatus.INACTIVE);

        // Business rule:
        // An INACTIVE agent is not eligible to process registrations.
        assertThat(agent.isEligibleForRegistration()).isFalse();
    }

    @Test
    void shouldActivateAgentSuccessfully() {

        // ------------------- Arrange -------------------
        // Create a new agent.
        // By default, the agent starts as INACTIVE.
        Agent agent = new Agent(
                AgentId.of("agent-1"),
                "Alice Brown",
                Role.CLERK
        );

        // ------------------- Act -------------------
        // Activate the agent.
        // Business rule: an agent can become ACTIVE.
        agent.activate();

        // ------------------- Assert -------------------
        // After activation, the agent status should be ACTIVE.
        assertThat(agent.status()).isEqualTo(AgentStatus.ACTIVE);

        // Business rule:
        // Only ACTIVE agents are eligible to process registrations.
        assertThat(agent.isEligibleForRegistration()).isTrue();
    }

    @Test
    void shouldUpdateAgentSuccessfully() {

        // ------------------- Arrange -------------------
        // Create an agent with initial name and role.
        Agent agent = new Agent(
                AgentId.of("agent-1"),
                "Alice Brown",
                Role.CLERK
        );

        // ------------------- Act -------------------
        // Update the agent's name and role.
        agent.update("Bob Martin", Role.SUPERVISOR);

        // ------------------- Assert -------------------
        // Verify that the name was updated.
        assertThat(agent.name()).isEqualTo("Bob Martin");

        // Verify that the role was updated.
        assertThat(agent.role()).isEqualTo(Role.SUPERVISOR);
    }

    @Test
    void shouldTrimAgentNameWhenCreated() {

        // ------------------- Act -------------------
        // Create an agent with extra spaces before and after the name.
        // Business rule: agent name should be normalized by trimming spaces.
        Agent agent = new Agent(
                AgentId.of("agent-1"),
                "  Alice Brown  ",
                Role.CLERK
        );

        // ------------------- Assert -------------------
        // The stored name should not contain the extra spaces.
        assertThat(agent.name()).isEqualTo("Alice Brown");
    }

    @Test
    void shouldTrimAgentNameWhenUpdated() {

        // ------------------- Arrange -------------------
        // Create an agent with a valid initial name.
        Agent agent = new Agent(
                AgentId.of("agent-1"),
                "Alice Brown",
                Role.CLERK
        );

        // ------------------- Act -------------------
        // Update the agent name with extra spaces.
        // Business rule: updated name should also be trimmed.
        agent.update("  Bob Martin  ", Role.SUPERVISOR);

        // ------------------- Assert -------------------
        // The stored updated name should not contain the extra spaces.
        assertThat(agent.name()).isEqualTo("Bob Martin");
    }

    @Test
    void shouldThrowExceptionWhenAgentNameIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: agent name cannot be null.
        // The constructor should reject null values.
        assertThrows(InvalidAgentNameException.class,
                () -> new Agent(AgentId.of("agent-1"), null, Role.CLERK));
    }

    @Test
    void shouldThrowExceptionWhenAgentNameIsBlank() {

        // ------------------- Act + Assert -------------------
        // Business rule: agent name cannot be blank.
        // A string with only spaces is not a valid name.
        assertThrows(InvalidAgentNameException.class,
                () -> new Agent(AgentId.of("agent-1"), "   ", Role.CLERK));
    }

    @Test
    void shouldThrowExceptionWhenAgentNameIsTooShort() {

        // ------------------- Act + Assert -------------------
        // Business rule: agent name must have a minimum length.
        // A single character is too short and should be rejected.
        assertThrows(InvalidAgentNameException.class,
                () -> new Agent(AgentId.of("agent-1"), "A", Role.CLERK));
    }

    @Test
    void shouldThrowExceptionWhenAgentNameIsTooLong() {

        // ------------------- Arrange -------------------
        // Create a name that is longer than the allowed maximum length.
        String longName = "A".repeat(121);

        // ------------------- Act + Assert -------------------
        // Business rule: agent name must not exceed the maximum length.
        // This long name should be rejected by the constructor.
        assertThrows(InvalidAgentNameException.class,
                () -> new Agent(AgentId.of("agent-1"), longName, Role.CLERK));
    }

    @Test
    void shouldThrowExceptionWhenRoleIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: agent role cannot be null.
        // The constructor should reject a missing role.
        assertThrows(InvalidRoleException.class,
                () -> new Agent(AgentId.of("agent-1"), "Alice Brown", null));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNameToInvalidValue() {

        // ------------------- Arrange -------------------
        // Create a valid agent first.
        Agent agent = new Agent(
                AgentId.of("agent-1"),
                "Alice Brown",
                Role.CLERK
        );

        // ------------------- Act + Assert -------------------
        // Business rule: updated agent name must also be valid.
        // Empty name should be rejected.
        assertThrows(InvalidAgentNameException.class,
                () -> agent.update("", Role.CLERK));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingRoleToNull() {

        // ------------------- Arrange -------------------
        // Create a valid agent first.
        Agent agent = new Agent(
                AgentId.of("agent-1"),
                "Alice Brown",
                Role.CLERK
        );

        // ------------------- Act + Assert -------------------
        // Business rule: updated role must also be valid.
        // Null role should be rejected.
        assertThrows(InvalidRoleException.class,
                () -> agent.update("Bob Martin", null));
    }

    @Test
    void shouldSupportBothRoles() {

        // ------------------- Assert -------------------
        // Verify that the Role enum contains CLERK.
        assertThat(Role.CLERK).isEqualTo(Role.valueOf("CLERK"));

        // Verify that the Role enum contains SUPERVISOR.
        assertThat(Role.SUPERVISOR).isEqualTo(Role.valueOf("SUPERVISOR"));
    }
}