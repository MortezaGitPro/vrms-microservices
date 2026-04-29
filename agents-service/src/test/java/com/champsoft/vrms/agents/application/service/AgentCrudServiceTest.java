package com.champsoft.vrms.agents.application.service;

import com.champsoft.vrms.agents.application.exception.AgentNotFoundException;
import com.champsoft.vrms.agents.application.exception.DuplicateAgentException;
import com.champsoft.vrms.agents.application.port.out.AgentRepositoryPort;
import com.champsoft.vrms.agents.domain.model.Agent;
import com.champsoft.vrms.agents.domain.model.AgentId;
import com.champsoft.vrms.agents.domain.model.AgentStatus;
import com.champsoft.vrms.agents.domain.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// Enable Mockito → this is a pure service test (NO Spring, NO DB)
@ExtendWith(MockitoExtension.class)
class AgentCrudServiceTest {

    // Mocked repository → replaces real database
    // We control what it returns in each test
    @Mock
    private AgentRepositoryPort repo;

    // Real service under test
    // Mockito injects the mock repository into this service
    @InjectMocks
    private AgentCrudService service;

    // Helper method → creates valid Agent objects
    // Avoids repeating object creation in every test
    private Agent sampleAgent(String id, String name, Role role) {
        return new Agent(AgentId.of(id), name, role);
    }

    @Nested
    @DisplayName("Create agent")
    class CreateAgentTests {

        @Test
        void shouldCreateAgentSuccessfully() {

            // ------------------- Arrange -------------------
            // Name does not exist → creation allowed
            when(repo.existsByName("Alice Brown")).thenReturn(false);

            // Simulate saving → return same object
            when(repo.save(any(Agent.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            Agent saved = service.create("Alice Brown", Role.CLERK);

            // ------------------- Assert -------------------
            // Basic checks
            assertThat(saved).isNotNull();
            assertThat(saved.id()).isNotNull();

            // Business data validation
            assertThat(saved.name()).isEqualTo("Alice Brown");
            assertThat(saved.role()).isEqualTo(Role.CLERK);

            // Domain rule → new agent starts INACTIVE
            assertThat(saved.status()).isEqualTo(AgentStatus.INACTIVE);

            // Verify flow → check duplicate then save
            verify(repo).existsByName("Alice Brown");
            verify(repo).save(any(Agent.class));
        }

        @Test
        void shouldSaveAgentWithExpectedValues() {

            // ------------------- Arrange -------------------
            when(repo.existsByName("Bob Martin")).thenReturn(false);
            when(repo.save(any(Agent.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            service.create("Bob Martin", Role.SUPERVISOR);

            // ------------------- Assert -------------------
            // Capture object passed to repository
            ArgumentCaptor<Agent> captor = ArgumentCaptor.forClass(Agent.class);
            verify(repo).save(captor.capture());

            Agent agent = captor.getValue();

            // Verify service created correct object BEFORE saving
            assertThat(agent.name()).isEqualTo("Bob Martin");
            assertThat(agent.role()).isEqualTo(Role.SUPERVISOR);
            assertThat(agent.status()).isEqualTo(AgentStatus.INACTIVE);
        }

        @Test
        void shouldThrowDuplicateAgentExceptionWhenAgentNameAlreadyExists() {

            // ------------------- Arrange -------------------
            // Name already exists → should fail
            when(repo.existsByName("Alice Brown")).thenReturn(true);

            // ------------------- Act + Assert -------------------
            assertThrows(DuplicateAgentException.class,
                    () -> service.create("Alice Brown", Role.CLERK));

            // Verify → no save should happen
            verify(repo).existsByName("Alice Brown");
            verify(repo, never()).save(any(Agent.class));
        }
    }

    @Nested
    @DisplayName("Read agent")
    class ReadAgentTests {

        @Test
        void shouldReturnAgentWhenGettingById() {

            // ------------------- Arrange -------------------
            Agent agent = sampleAgent("agent-1", "Alice Brown", Role.CLERK);
            when(repo.findById(AgentId.of("agent-1")))
                    .thenReturn(Optional.of(agent));

            // ------------------- Act -------------------
            Agent found = service.getById("agent-1");

            // ------------------- Assert -------------------
            assertThat(found).isSameAs(agent);

            // Verify repository call
            verify(repo).findById(AgentId.of("agent-1"));
        }

        @Test
        void shouldThrowAgentNotFoundExceptionWhenGettingMissingAgent() {

            // ------------------- Arrange -------------------
            when(repo.findById(AgentId.of("missing-agent")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(AgentNotFoundException.class,
                    () -> service.getById("missing-agent"));

            verify(repo).findById(AgentId.of("missing-agent"));
        }

        @Test
        void shouldReturnAllAgents() {

            // ------------------- Arrange -------------------
            List<Agent> agents = List.of(
                    sampleAgent("agent-1", "Alice Brown", Role.CLERK),
                    sampleAgent("agent-2", "Bob Martin", Role.SUPERVISOR)
            );
            when(repo.findAll()).thenReturn(agents);

            // ------------------- Act -------------------
            List<Agent> result = service.list();

            // ------------------- Assert -------------------
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyElementsOf(agents);

            verify(repo).findAll();
        }
    }

    @Nested
    @DisplayName("Update agent")
    class UpdateAgentTests {

        @Test
        void shouldUpdateAgentSuccessfully() {

            // ------------------- Arrange -------------------
            Agent agent = sampleAgent("agent-1", "Alice Brown", Role.CLERK);

            when(repo.findById(AgentId.of("agent-1")))
                    .thenReturn(Optional.of(agent));

            when(repo.save(any(Agent.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            Agent updated = service.update("agent-1", "Bob Martin", Role.SUPERVISOR);

            // ------------------- Assert -------------------
            assertThat(updated.name()).isEqualTo("Bob Martin");
            assertThat(updated.role()).isEqualTo(Role.SUPERVISOR);

            // Verify correct flow
            verify(repo).findById(AgentId.of("agent-1"));
            verify(repo).save(agent);
        }

        @Test
        void shouldThrowAgentNotFoundExceptionWhenUpdatingMissingAgent() {

            // ------------------- Arrange -------------------
            when(repo.findById(AgentId.of("missing-agent")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(AgentNotFoundException.class,
                    () -> service.update("missing-agent", "Bob Martin", Role.SUPERVISOR));

            verify(repo).findById(AgentId.of("missing-agent"));
            verify(repo, never()).save(any(Agent.class));
        }
    }

    @Nested
    @DisplayName("Activate agent")
    class ActivateAgentTests {

        @Test
        void shouldActivateAgentSuccessfully() {

            // ------------------- Arrange -------------------
            Agent agent = sampleAgent("agent-1", "Alice Brown", Role.CLERK);

            when(repo.findById(AgentId.of("agent-1")))
                    .thenReturn(Optional.of(agent));

            when(repo.save(any(Agent.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // ------------------- Act -------------------
            Agent activated = service.activate("agent-1");

            // ------------------- Assert -------------------
            // Business rule → ACTIVE agents are eligible
            assertThat(activated.status()).isEqualTo(AgentStatus.ACTIVE);
            assertThat(activated.isEligibleForRegistration()).isTrue();

            verify(repo).findById(AgentId.of("agent-1"));
            verify(repo).save(agent);
        }

        @Test
        void shouldThrowAgentNotFoundExceptionWhenActivatingMissingAgent() {

            // ------------------- Arrange -------------------
            when(repo.findById(AgentId.of("missing-agent")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(AgentNotFoundException.class,
                    () -> service.activate("missing-agent"));

            verify(repo).findById(AgentId.of("missing-agent"));
            verify(repo, never()).save(any(Agent.class));
        }
    }

    @Nested
    @DisplayName("Delete agent")
    class DeleteAgentTests {

        @Test
        void shouldDeleteAgentSuccessfully() {

            // ------------------- Arrange -------------------
            Agent agent = sampleAgent("agent-1", "Alice Brown", Role.CLERK);

            when(repo.findById(AgentId.of("agent-1")))
                    .thenReturn(Optional.of(agent));

            // ------------------- Act -------------------
            service.delete("agent-1");

            // ------------------- Assert -------------------
            // Flow → find first, then delete
            verify(repo).findById(AgentId.of("agent-1"));
            verify(repo).deleteById(AgentId.of("agent-1"));
        }

        @Test
        void shouldThrowAgentNotFoundExceptionWhenDeletingMissingAgent() {

            // ------------------- Arrange -------------------
            when(repo.findById(AgentId.of("missing-agent")))
                    .thenReturn(Optional.empty());

            // ------------------- Act + Assert -------------------
            assertThrows(AgentNotFoundException.class,
                    () -> service.delete("missing-agent"));

            verify(repo).findById(AgentId.of("missing-agent"));
            verify(repo, never()).deleteById(any(AgentId.class));
        }
    }
}