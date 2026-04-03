package com.champsoft.vrms.agents.domain.model;

import com.champsoft.vrms.agents.domain.exception.InvalidAgentNameException;
import com.champsoft.vrms.agents.domain.exception.InvalidRoleException;

public class Agent {
    private final AgentId id;
    private String name;
    private Role role;
    private AgentStatus status;

    public Agent(AgentId id, String name, Role role) {
        this.id = id;
        setName(name);
        setRole(role);
        this.status = AgentStatus.INACTIVE;
    }

    public AgentId id() { return id; }
    public String name() { return name; }
    public Role role() { return role; }
    public AgentStatus status() { return status; }

    public void update(String name, Role role) {
        setName(name);
        setRole(role);
    }

    public void activate() { this.status = AgentStatus.ACTIVE; }

    public boolean isEligibleForRegistration() { return status == AgentStatus.ACTIVE; }

    private void setName(String name) {
        if (name == null) throw new InvalidAgentNameException("Agent name is required");
        String v = name.trim();
        if (v.length() < 2 || v.length() > 120) throw new InvalidAgentNameException("Agent name length must be 2..120");
        this.name = v;
    }

    private void setRole(Role role) {
        if (role == null) throw new InvalidRoleException("Role is required");
        this.role = role;
    }
}
