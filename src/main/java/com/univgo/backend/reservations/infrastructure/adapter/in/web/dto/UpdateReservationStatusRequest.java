package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReservationStatusRequest(@NotNull ReservationStatus status) {
}
