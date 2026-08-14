package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CreateReservationRequest(
        @NotNull UUID spaceId,
        @NotNull LocalDate reservationDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        List<String> guestIdentifications) {
}
