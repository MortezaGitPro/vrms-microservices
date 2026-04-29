package com.champsoft.vrms.agents.application.service;

import com.champsoft.vrms.agents.application.exception.AgentNotFoundException;
import com.champsoft.vrms.agents.application.port.out.AgentRepositoryPort;
import com.champsoft.vrms.agents.domain.model.Agent;
import com.champsoft.vrms.agents.domain.model.AgentId;
import com.champsoft.vrms.agents.domain.model.Role;
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

// Enable Mockito → pure service test (NO Spring, NO DB, NO HTTP)
@ExtendWith(MockitoExtension.class)
class AgentEligibilityServiceTest {

    // Mocked repository → replaces real database access
    // We fully control what this returns in each test
    @Mock
    private AgentRepositoryPort repo;

    // Real service under test
    // Mockito injects the mocked repository into this service
    @InjectMocks
    private AgentEligibilityService service;

    // Helper method → creates a valid Agent object
    // Default state = INACTIVE (important for eligibility logic)
    private Agent sampleAgent() {
        return new Agent(
                AgentId.of("agent-1"),
                "Alice Brown",
                Role.CLERK
        );
    }

    @Test
    void shouldReturnTrueWhenAgentIsActive() {

        // ------------------- Arrange -------------------
        // Create agent and activate it
        // Business rule: ACTIVE → eligible
        Agent agent = sampleAgent();
        agent.activate();

        // Mock repository → return this active agent
        when(repo.findById(AgentId.of("agent-1")))
                .thenReturn(Optional.of(agent));

        // ------------------- Act -------------------
        // Call service method
        boolean result = service.isEligible("agent-1");

        // ------------------- Assert -------------------
        // ACTIVE agent should be eligible
        assertThat(result).isTrue();

        // Verify repository lookup happened
        verify(repo).findById(AgentId.of("agent-1"));
    }

    @Test
    void shouldReturnFalseWhenAgentIsInactive() {

        // ------------------- Arrange -------------------
        // Create agent but DO NOT activate it
        // Default state = INACTIVE
        Agent agent = sampleAgent();

        // Mock repository → return inactive agent
        when(repo.findById(AgentId.of("agent-1")))
                .thenReturn(Optional.of(agent));

        // ------------------- Act -------------------
        boolean result = service.isEligible("agent-1");

        // ------------------- Assert -------------------
        // INACTIVE agent should NOT be eligible
        assertThat(result).isFalse();

        // Verify repository call
        verify(repo).findById(AgentId.of("agent-1"));
    }

    @Test
    void shouldThrowAgentNotFoundExceptionWhenAgentDoesNotExist() {

        // ------------------- Arrange -------------------
        // Repository returns empty → agent not found
        when(repo.findById(AgentId.of("missing-agent")))
                .thenReturn(Optional.empty());

        // ------------------- Act + Assert -------------------
        // Service should throw exception when agent is missing
        assertThrows(AgentNotFoundException.class,
                () -> service.isEligible("missing-agent"));

        // Verify repository lookup was attempted
        verify(repo).findById(AgentId.of("missing-agent"));
    }
}