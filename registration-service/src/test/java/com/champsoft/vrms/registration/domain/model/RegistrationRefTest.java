package com.champsoft.vrms.registration.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class RegistrationRefTest {

    @Test
    void shouldCreateVehicleRef() {

        // ------------------- Act -------------------
        // Create a VehicleRef value object with a valid vehicle ID.
        VehicleRef ref = new VehicleRef("vehicle-1");

        // ------------------- Assert -------------------
        // Verify that the vehicle reference stores the expected value.
        assertThat(ref.value()).isEqualTo("vehicle-1");
    }

    @Test
    void shouldTrimVehicleRef() {

        // ------------------- Act -------------------
        // Create a VehicleRef with extra spaces before and after the value.
        // Business rule: reference values should be normalized by trimming spaces.
        VehicleRef ref = new VehicleRef("  vehicle-1  ");

        // ------------------- Assert -------------------
        // The stored value should not contain the extra spaces.
        assertThat(ref.value()).isEqualTo("vehicle-1");
    }

    @Test
    void shouldThrowExceptionWhenVehicleRefIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: vehicle reference cannot be null.
        // A registration must always reference a vehicle.
        assertThrows(IllegalArgumentException.class, () -> new VehicleRef(null));
    }

    @Test
    void shouldThrowExceptionWhenVehicleRefIsBlank() {

        // ------------------- Act + Assert -------------------
        // Business rule: vehicle reference cannot be blank.
        // A string with only spaces is not a valid vehicle reference.
        assertThrows(IllegalArgumentException.class, () -> new VehicleRef("   "));
    }

    @Test
    void shouldCreateOwnerRef() {

        // ------------------- Act -------------------
        // Create an OwnerRef value object with a valid owner ID.
        OwnerRef ref = new OwnerRef("owner-1");

        // ------------------- Assert -------------------
        // Verify that the owner reference stores the expected value.
        assertThat(ref.value()).isEqualTo("owner-1");
    }

    @Test
    void shouldTrimOwnerRef() {

        // ------------------- Act -------------------
        // Create an OwnerRef with extra spaces before and after the value.
        // Business rule: reference values should be normalized by trimming spaces.
        OwnerRef ref = new OwnerRef("  owner-1  ");

        // ------------------- Assert -------------------
        // The stored value should not contain the extra spaces.
        assertThat(ref.value()).isEqualTo("owner-1");
    }

    @Test
    void shouldThrowExceptionWhenOwnerRefIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: owner reference cannot be null.
        // A registration must always reference an owner.
        assertThrows(IllegalArgumentException.class, () -> new OwnerRef(null));
    }

    @Test
    void shouldThrowExceptionWhenOwnerRefIsBlank() {

        // ------------------- Act + Assert -------------------
        // Business rule: owner reference cannot be blank.
        // A string with only spaces is not a valid owner reference.
        assertThrows(IllegalArgumentException.class, () -> new OwnerRef("   "));
    }

    @Test
    void shouldCreateAgentRef() {

        // ------------------- Act -------------------
        // Create an AgentRef value object with a valid agent ID.
        AgentRef ref = new AgentRef("agent-1");

        // ------------------- Assert -------------------
        // Verify that the agent reference stores the expected value.
        assertThat(ref.value()).isEqualTo("agent-1");
    }

    @Test
    void shouldTrimAgentRef() {

        // ------------------- Act -------------------
        // Create an AgentRef with extra spaces before and after the value.
        // Business rule: reference values should be normalized by trimming spaces.
        AgentRef ref = new AgentRef("  agent-1  ");

        // ------------------- Assert -------------------
        // The stored value should not contain the extra spaces.
        assertThat(ref.value()).isEqualTo("agent-1");
    }

    @Test
    void shouldThrowExceptionWhenAgentRefIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: agent reference cannot be null.
        // A registration must always reference the agent who processed it.
        assertThrows(IllegalArgumentException.class, () -> new AgentRef(null));
    }

    @Test
    void shouldThrowExceptionWhenAgentRefIsBlank() {

        // ------------------- Act + Assert -------------------
        // Business rule: agent reference cannot be blank.
        // A string with only spaces is not a valid agent reference.
        assertThrows(IllegalArgumentException.class, () -> new AgentRef("   "));
    }
}