package com.champsoft.vrms.agents.application.service;

import com.champsoft.vrms.agents.application.exception.*;
import com.champsoft.vrms.agents.application.port.out.AgentRepositoryPort;
import com.champsoft.vrms.agents.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AgentCrudService {

    private final AgentRepositoryPort repo;

    public AgentCrudService(AgentRepositoryPort repo) {
        this.repo = repo;
    }

    @Transactional
    public Agent create(String name, Role role) {
        var candidate = new Agent(AgentId.newId(), name, role);
        if (repo.existsByName(candidate.name())) {
            throw new DuplicateAgentException("Agent already exists by name: " + candidate.name());
        }
        return repo.save(candidate);
    }

    @Transactional(readOnly = true)
    public Agent getById(String id) {
        return repo.findById(AgentId.of(id))
                .orElseThrow(() -> new AgentNotFoundException("Agent not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Agent> list() { return repo.findAll(); }

    @Transactional
    public Agent update(String id, String name, Role role) {
        var a = getById(id);
        a.update(name, role);
        return repo.save(a);
    }

    @Transactional
    public Agent activate(String id) {
        var a = getById(id);
        a.activate();
        return repo.save(a);
    }

    @Transactional
    public void delete(String id) {
        getById(id);
        repo.deleteById(AgentId.of(id));
    }
}
