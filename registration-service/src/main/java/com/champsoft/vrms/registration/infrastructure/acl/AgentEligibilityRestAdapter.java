package com.champsoft.vrms.registration.infrastructure.acl;

import com.champsoft.vrms.registration.application.exception.CrossContextValidationException;
import com.champsoft.vrms.registration.application.port.out.AgentEligibilityPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AgentEligibilityRestAdapter implements AgentEligibilityPort {

    private final RestTemplate restTemplate;

    @Value("${services.agents.base-url}")
    private String agentsBaseUrl;

    public AgentEligibilityRestAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isEligible(String agentId) {
        String url = agentsBaseUrl + "/api/agents/" + agentId + "/eligibility";

        try {
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CrossContextValidationException("Agent not found: " + agentId);
        } catch (HttpClientErrorException ex) {
            throw new CrossContextValidationException("Agent validation failed: " + agentId);
        } catch (Exception ex) {
            throw new CrossContextValidationException("Agent service is unavailable.");
        }
    }
}