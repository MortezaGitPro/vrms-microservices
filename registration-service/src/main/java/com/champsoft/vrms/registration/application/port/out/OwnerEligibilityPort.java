package com.champsoft.vrms.registration.application.port.out;

public interface OwnerEligibilityPort {
    boolean isEligible(String ownerId);
}
