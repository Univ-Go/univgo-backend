package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AdminCreateReservationRequest(
        @NotBlank String studentIdentifier,
        @NotNull UUID spaceId,
        @NotNull LocalDate reservationDate,
        @NotNull LocalTime startTime) {
}
