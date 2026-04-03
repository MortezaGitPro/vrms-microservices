package com.champsoft.vrms.agents.application.port.out;

import com.champsoft.vrms.agents.domain.model.Agent;
import com.champsoft.vrms.agents.domain.model.AgentId;

import java.util.List;
import java.util.Optional;

public interface AgentRepositoryPort {
    Agent save(Agent agent);
    Optional<Agent> findById(AgentId id);
    boolean existsByName(String name);
    List<Agent> findAll();
    void deleteById(AgentId id);
}
