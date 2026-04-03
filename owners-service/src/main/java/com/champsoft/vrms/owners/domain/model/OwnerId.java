package com.champsoft.vrms.owners.domain.model;

import java.util.Objects;
import java.util.UUID;

public final class OwnerId {
    private final String value;

    private OwnerId(String value) { this.value = value; }

    public static OwnerId newId() { return new OwnerId(UUID.randomUUID().toString()); }
    public static OwnerId of(String value) { return new OwnerId(value); }
    public String value() { return value; }

    @Override public boolean equals(Object o) {
        return (o instanceof OwnerId other) && Objects.equals(value, other.value);
    }
    @Override public int hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
