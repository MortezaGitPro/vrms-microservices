package com.champsoft.vrms.agents.infrastructure.persistence;

import com.champsoft.vrms.agents.application.port.out.AgentRepositoryPort;
import com.champsoft.vrms.agents.domain.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaAgentRepositoryAdapter implements AgentRepositoryPort {

    private final SpringDataAgentRepository jpa;
    public JpaAgentRepositoryAdapter(SpringDataAgentRepository jpa) { this.jpa = jpa; }

    @Override
    public Agent save(Agent agent) {
        jpa.save(toEntity(agent));
        return agent;
    }

    @Override
    public Optional<Agent> findById(AgentId id) {
        return jpa.findById(id.value()).map(this::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return jpa.existsByNameIgnoreCase(name);
    }

    @Override
    public List<Agent> findAll() { return jpa.findAll().stream().map(this::toDomain).toList(); }

    @Override
    public void deleteById(AgentId id) { jpa.deleteById(id.value()); }

    private AgentJpaEntity toEntity(Agent a) {
        var e = new AgentJpaEntity();
        e.id = a.id().value();
        e.name = a.name();
        e.role = a.role().name();
        e.status = a.status().name();
        return e;
    }

    private Agent toDomain(AgentJpaEntity e) {
        var a = new Agent(AgentId.of(e.id), e.name, Role.valueOf(e.role));
        if ("ACTIVE".equalsIgnoreCase(e.status)) a.activate();
        return a;
    }
}
