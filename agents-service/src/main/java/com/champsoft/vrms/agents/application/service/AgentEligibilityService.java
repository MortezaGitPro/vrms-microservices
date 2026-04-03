package com.champsoft.vrms.agents.application.service;

import com.champsoft.vrms.agents.application.exception.AgentNotFoundException;
import com.champsoft.vrms.agents.application.port.out.AgentRepositoryPort;
import com.champsoft.vrms.agents.domain.model.AgentId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentEligibilityService {

    private final AgentRepositoryPort repo;
    public AgentEligibilityService(AgentRepositoryPort repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public boolean isEligible(String agentId) {
        return repo.findById(AgentId.of(agentId))
                .map(a -> a.isEligibleForRegistration())
                .orElseThrow(() -> new AgentNotFoundException("Agent not found: " + agentId));
    }
}
