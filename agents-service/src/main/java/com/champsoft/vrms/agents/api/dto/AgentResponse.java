package com.champsoft.vrms.agents.api.dto;

public record AgentResponse(
        String id,
        String name,
        String role,
        String status) {}
