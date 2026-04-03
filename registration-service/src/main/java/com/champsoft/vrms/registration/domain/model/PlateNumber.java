package com.champsoft.vrms.registration.domain.model;

import com.champsoft.vrms.registration.domain.exception.InvalidPlateException;

public final class PlateNumber {
    private final String value;

    public PlateNumber(String value) {
        if (value == null) throw new InvalidPlateException("Plate is required");
        String v = value.trim().toUpperCase();
        if (v.length() < 3 || v.length() > 10) throw new InvalidPlateException("Plate length must be 3..10");
        this.value = v;
    }

    public String value() { return value; }
}
