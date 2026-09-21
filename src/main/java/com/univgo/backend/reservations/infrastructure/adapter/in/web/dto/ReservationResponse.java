package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationState;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        String qrCodeData,
        UUID userId,
        UUID spaceId,
        LocalDate reservationDate,
        LocalTime blockStart,
        LocalTime blockEnd,
        ReservationState state,
        LocalDateTime createdAt,
        LocalDateTime checkedInAt,
        LocalDateTime cancelledAt,
        LocalDateTime checkInOpensAt,
        LocalDateTime checkInClosesAt,
        LocalDateTime cancellationDeadline,
        boolean cancellable) {

    // Computed fresh on every read from the current "now" — never cached, per the
    // spec's "la autoridad es el servidor".
    public static ReservationResponse from(Reservation reservation, InstitutionConfig config) {
        LocalDateTime now = LocalDateTime.now();
        ReservationState state = reservation.stateAt(now, config.tolerance(), config.minUsage());
        LocalDateTime cancellationDeadline = reservation.cancellationDeadline();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getQrCodeData(),
                reservation.getUserId(),
                reservation.getSpaceId(),
                reservation.getReservationDate(),
                reservation.getBlockStart(),
                reservation.getBlockEnd(),
                state,
                reservation.getCreatedAt(),
                reservation.getCheckedInAt(),
                reservation.getCancelledAt(),
                reservation.checkInOpensAt(config.tolerance()),
                reservation.checkInClosesAt(config.tolerance(), config.minUsage()),
                cancellationDeadline,
                state == ReservationState.RESERVED && !now.isAfter(cancellationDeadline));
    }
}
