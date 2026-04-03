package com.champsoft.vrms.agents.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class AgentId {
    private final String value;

    private AgentId(String value) { this.value = value; }

    public static AgentId newId() { return new AgentId(UUID.randomUUID().toString()); }
    public static AgentId of(String value) { return new AgentId(value); }
    public String value() { return value; }

    @Override public boolean equals(Object o) {
        return (o instanceof AgentId other) && Objects.equals(value, other.value);
    }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
