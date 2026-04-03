package com.champsoft.vrms.registration.domain.model;

import java.util.UUID;

public final class RegistrationId {
    private final String value;

    private RegistrationId(String value) { this.value = value; }

    public static RegistrationId newId() { return new RegistrationId(UUID.randomUUID().toString()); }
    public static RegistrationId of(String value) { return new RegistrationId(value); }
    public String value() { return value; }
}
