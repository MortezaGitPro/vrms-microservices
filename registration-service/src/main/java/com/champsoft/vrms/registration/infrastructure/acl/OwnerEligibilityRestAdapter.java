package com.champsoft.vrms.registration.infrastructure.acl;

import com.champsoft.vrms.registration.application.exception.CrossContextValidationException;
import com.champsoft.vrms.registration.application.port.out.OwnerEligibilityPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class OwnerEligibilityRestAdapter implements OwnerEligibilityPort {

    private final RestTemplate restTemplate;

    @Value("${services.owners.base-url}")
    private String ownersBaseUrl;

    public OwnerEligibilityRestAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isEligible(String ownerId) {
        String url = ownersBaseUrl + "/api/owners/" + ownerId + "/eligibility";

        try {
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CrossContextValidationException("Owner not found: " + ownerId);
        } catch (HttpClientErrorException ex) {
            throw new CrossContextValidationException("Owner validation failed: " + ownerId);
        } catch (Exception ex) {
            throw new CrossContextValidationException("Owner service is unavailable.");
        }
    }
}