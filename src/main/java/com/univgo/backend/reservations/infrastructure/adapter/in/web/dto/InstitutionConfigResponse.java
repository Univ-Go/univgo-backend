package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.InstitutionConfig;

public record InstitutionConfigResponse(
        int blockDurationMinutes, int checkInToleranceMinutes, int minUsageMinutes, int reservationsPerSpacePerDay) {

    public static InstitutionConfigResponse from(InstitutionConfig config) {
        return new InstitutionConfigResponse(
                config.getBlockDurationMinutes(),
                config.getCheckInToleranceMinutes(),
                config.getMinUsageMinutes(),
                config.getReservationsPerSpacePerDay());
    }
}
