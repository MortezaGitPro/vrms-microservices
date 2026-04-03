package com.champsoft.vrms.registration.domain.model;

import java.time.LocalDate;

public record ExpiryDate(LocalDate value) {

    public ExpiryDate {
        if (value == null) {
            throw new IllegalArgumentException("expiry is required");
        }
    }

    public boolean isFuture() {
        return value.isAfter(LocalDate.now());
    }

    public boolean isExpired() {
        return !value.isAfter(LocalDate.now());
    }
}
