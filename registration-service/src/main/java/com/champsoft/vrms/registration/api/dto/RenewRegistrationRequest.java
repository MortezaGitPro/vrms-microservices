package com.champsoft.vrms.registration.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RenewRegistrationRequest(@NotNull LocalDate newExpiry) {}
