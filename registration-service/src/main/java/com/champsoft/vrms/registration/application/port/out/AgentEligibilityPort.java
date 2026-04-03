package com.champsoft.vrms.registration.application.port.out;

public interface AgentEligibilityPort {
    boolean isEligible(String agentId);
}
