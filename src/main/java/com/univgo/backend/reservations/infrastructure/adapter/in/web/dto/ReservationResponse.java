package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.CancelledBy;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.reservations.domain.ReservationStatus;
import com.univgo.backend.reservations.domain.ReservationStatusResolver;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceClosures;
import java.time.Clock;
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
        CancelledBy cancelledBy,
        ClosureReason closureReason,
        LocalDateTime checkInOpensAt,
        LocalDateTime checkInClosesAt,
        LocalDateTime cancellationDeadline,
        boolean cancellable) {

    // Computed fresh on every read from the current "now" — never cached, per the
    // spec's "la autoridad es el servidor".
    //
    // cancelledBy and closureReason travel with it because a booking the student gave up and one the
    // university took away read identically without them, and only one of the two is their doing.
    // Suspension is resolved here too: it is the reservation's clock read against the space's
    // closures, and neither of the two is stored (spec §12).
    public static ReservationResponse from(
            Reservation reservation, InstitutionConfig config, SpaceClosures closures, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        ReservationStatus status = ReservationStatusResolver.resolve(reservation, closures, now, config);
        LocalDateTime cancellationDeadline = reservation.cancellationDeadline();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getQrCodeData(),
                reservation.getUserId(),
                reservation.getSpaceId(),
                reservation.getReservationDate(),
                reservation.getBlockStart(),
                reservation.getBlockEnd(),
                status.state(),
                reservation.getCreatedAt(),
                reservation.getCheckedInAt(),
                reservation.getCancelledAt(),
                status.cancelledBy(),
                status.closureReason(),
                reservation.checkInOpensAt(config.tolerance()),
                reservation.checkInClosesAt(config.tolerance(), config.minUsage()),
                cancellationDeadline,
                status.state() == ReservationState.RESERVED && !now.isAfter(cancellationDeadline));
    }
}
