package com.champsoft.vrms.agents.api.mapper;

import com.champsoft.vrms.agents.api.dto.AgentResponse;
import com.champsoft.vrms.agents.domain.model.Agent;

public class AgentApiMapper {
    public static AgentResponse toResponse(Agent a) {
        return new AgentResponse(
                a.id().value(),
                a.name(),
                a.role().name(),
                a.status().name()
        );
    }
}
