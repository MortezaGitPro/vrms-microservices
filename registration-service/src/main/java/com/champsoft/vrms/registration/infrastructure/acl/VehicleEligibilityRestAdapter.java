package com.champsoft.vrms.registration.infrastructure.acl;

import com.champsoft.vrms.registration.application.exception.CrossContextValidationException;
import com.champsoft.vrms.registration.application.port.out.VehicleEligibilityPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class VehicleEligibilityRestAdapter implements VehicleEligibilityPort {

    private final RestTemplate restTemplate;

    @Value("${services.cars.base-url}")
    private String carsBaseUrl;

    public VehicleEligibilityRestAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean isEligible(String vehicleId) {
        String url = carsBaseUrl + "/api/cars/" + vehicleId + "/eligibility";

        try {
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new CrossContextValidationException("Vehicle not found: " + vehicleId);
        } catch (HttpClientErrorException ex) {
            throw new CrossContextValidationException("Vehicle validation failed: " + vehicleId);
        } catch (Exception ex) {
            throw new CrossContextValidationException("Vehicle service is unavailable.");
        }
    }
}