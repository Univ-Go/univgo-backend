package com.univgo.backend.reservations.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * A reservation stores only the raw facts a human action produces: creation,
 * check-in, cancellation. Its state is never one of them — it's derived on
 * every read by {@link ReservationTimingCalculator#stateAt}, per the spec's
 * "el estado se calcula, no se guarda."
 */
public class Reservation {

    private final UUID id;
    private final String qrCodeData;
    private final UUID userId;
    private final UUID spaceId;
    private final LocalDate reservationDate;
    private final LocalTime blockStart;
    private final LocalTime blockEnd;
    private final LocalDateTime createdAt;
    private LocalDateTime checkedInAt;
    private LocalDateTime cancelledAt;
    private CancelledBy cancelledBy;

    public Reservation(
            UUID id,
            String qrCodeData,
            UUID userId,
            UUID spaceId,
            LocalDate reservationDate,
            LocalTime blockStart,
            LocalTime blockEnd,
            LocalDateTime createdAt,
            LocalDateTime checkedInAt,
            LocalDateTime cancelledAt,
            CancelledBy cancelledBy) {
        if (!blockEnd.isAfter(blockStart)) {
            throw new IllegalArgumentException("blockEnd must be after blockStart");
        }
        this.id = id;
        this.qrCodeData = qrCodeData;
        this.userId = userId;
        this.spaceId = spaceId;
        this.reservationDate = reservationDate;
        this.blockStart = blockStart;
        this.blockEnd = blockEnd;
        this.createdAt = createdAt;
        this.checkedInAt = checkedInAt;
        this.cancelledAt = cancelledAt;
        this.cancelledBy = cancelledBy;
    }

    public ReservationState stateAt(LocalDateTime now, Duration tolerance, Duration minUsage) {
        return ReservationTimingCalculator.stateAt(
                now, blockStartDateTime(), blockEndDateTime(), createdAt, checkedInAt, cancelledAt, tolerance, minUsage);
    }

    public void cancel(CancelledBy actor, LocalDateTime now, Duration tolerance, Duration minUsage) {
        ReservationState current = stateAt(now, tolerance, minUsage);
        if (current == ReservationState.IN_PROGRESS) {
            throw new CannotCancelInProgressReservationException(id);
        }
        if (current != ReservationState.RESERVED) {
            throw new InvalidReservationStateException(current);
        }
        this.cancelledAt = now;
        this.cancelledBy = actor;
    }

    public void checkIn(LocalDateTime now) {
        this.checkedInAt = now;
    }

    public LocalDateTime checkInOpensAt(Duration tolerance) {
        return ReservationTimingCalculator.checkInOpensAt(blockStartDateTime(), createdAt, tolerance);
    }

    public LocalDateTime checkInClosesAt(Duration tolerance, Duration minUsage) {
        return ReservationTimingCalculator.checkInClosesAt(blockStartDateTime(), blockEndDateTime(), createdAt, tolerance, minUsage);
    }

    private LocalDateTime blockStartDateTime() {
        return LocalDateTime.of(reservationDate, blockStart);
    }

    private LocalDateTime blockEndDateTime() {
        return LocalDateTime.of(reservationDate, blockEnd);
    }

    public UUID getId() {
        return id;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public LocalTime getBlockStart() {
        return blockStart;
    }

    public LocalTime getBlockEnd() {
        return blockEnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public CancelledBy getCancelledBy() {
        return cancelledBy;
    }
}
