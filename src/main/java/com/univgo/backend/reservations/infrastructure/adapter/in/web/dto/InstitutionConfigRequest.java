package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.InstitutionConfig;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InstitutionConfigRequest(
        @NotNull @Min(1) Integer blockDurationMinutes,
        @NotNull @Min(0) Integer checkInToleranceMinutes,
        @NotNull @Min(0) Integer minUsageMinutes,
        @NotNull @Min(1) Integer reservationsPerSpacePerDay) {

    public InstitutionConfig toDomain() {
        return new InstitutionConfig(blockDurationMinutes, checkInToleranceMinutes, minUsageMinutes, reservationsPerSpacePerDay);
    }
}
