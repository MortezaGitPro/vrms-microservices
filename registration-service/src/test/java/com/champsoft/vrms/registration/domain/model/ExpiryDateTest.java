package com.champsoft.vrms.registration.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Domain test → pure business rule testing
// NO Spring, NO Mockito, NO database
class ExpiryDateTest {

    @Test
    void shouldCreateValidExpiryDate() {

        // ------------------- Arrange -------------------
        // Create a future date.
        // Business rule: a registration expiry date is normally expected to be in the future.
        LocalDate date = LocalDate.now().plusYears(1);

        // ------------------- Act -------------------
        // Create an ExpiryDate value object using the future date.
        ExpiryDate expiryDate = new ExpiryDate(date);

        // ------------------- Assert -------------------
        // Verify that the expiry date stores the expected LocalDate value.
        assertThat(expiryDate.value()).isEqualTo(date);
    }

    @Test
    void shouldThrowExceptionWhenExpiryDateIsNull() {

        // ------------------- Act + Assert -------------------
        // Business rule: expiry date cannot be null.
        // The constructor should reject null values.
        assertThrows(IllegalArgumentException.class, () -> new ExpiryDate(null));
    }

    @Test
    void shouldReturnTrueWhenExpiryDateIsFuture() {

        // ------------------- Arrange -------------------
        // Create an expiry date for tomorrow.
        ExpiryDate expiryDate = new ExpiryDate(LocalDate.now().plusDays(1));

        // ------------------- Assert -------------------
        // Tomorrow is a future date.
        assertThat(expiryDate.isFuture()).isTrue();

        // A future expiry date is not expired.
        assertThat(expiryDate.isExpired()).isFalse();
    }

    @Test
    void shouldReturnExpiredWhenExpiryDateIsToday() {

        // ------------------- Arrange -------------------
        // Create an expiry date for today.
        ExpiryDate expiryDate = new ExpiryDate(LocalDate.now());

        // ------------------- Assert -------------------
        // Today is not considered a future date.
        assertThat(expiryDate.isFuture()).isFalse();

        // Business rule:
        // If the expiry date is today, it is considered expired.
        assertThat(expiryDate.isExpired()).isTrue();
    }

    @Test
    void shouldReturnExpiredWhenExpiryDateIsPast() {

        // ------------------- Arrange -------------------
        // Create an expiry date for yesterday.
        ExpiryDate expiryDate = new ExpiryDate(LocalDate.now().minusDays(1));

        // ------------------- Assert -------------------
        // A past date is not in the future.
        assertThat(expiryDate.isFuture()).isFalse();

        // A past expiry date is expired.
        assertThat(expiryDate.isExpired()).isTrue();
    }
}