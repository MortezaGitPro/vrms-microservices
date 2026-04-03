package com.champsoft.vrms.owners.api.dto;

public record OwnerResponse(
        String id,
        String fullName,
        String address,
        String status) {}
