package com.champsoft.vrms.owners.domain.model;

import com.champsoft.vrms.owners.domain.exception.InvalidAddressException;

public final class Address {
    private final String value;

    public Address(String value) {
        if (value == null) {
            this.value = null; // allow null address to keep it minimal
            return;
        }
        String v = value.trim();
        if (v.length() > 200) throw new InvalidAddressException("Address max length is 200");
        this.value = v.isEmpty() ? null : v;
    }

    public String value() { return value; }
}
