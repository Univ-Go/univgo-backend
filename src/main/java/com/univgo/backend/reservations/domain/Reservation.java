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
    private final ReservationSchedule schedule;
    private final LocalDateTime createdAt;
    private ReservationCheckpoint checkpoint;

    public Reservation(
            UUID id,
            String qrCodeData,
            UUID userId,
            UUID spaceId,
            ReservationSchedule schedule,
            LocalDateTime createdAt,
            ReservationCheckpoint checkpoint) {
        this.id = id;
        this.qrCodeData = qrCodeData;
        this.userId = userId;
        this.spaceId = spaceId;
        this.schedule = schedule;
        this.createdAt = createdAt;
        this.checkpoint = checkpoint;
    }

    public ReservationState stateAt(LocalDateTime now, Duration tolerance, Duration minUsage) {
        return ReservationTimingCalculator.stateAt(
                now,
                schedule.startDateTime(),
                schedule.endDateTime(),
                createdAt,
                checkpoint.checkedInAt(),
                checkpoint.cancelledAt(),
                new TimingRules(tolerance, minUsage));
    }

    public void cancel(CancelledBy actor, LocalDateTime now, Duration tolerance, Duration minUsage) {
        ReservationState current = stateAt(now, tolerance, minUsage);
        if (current == ReservationState.IN_PROGRESS) {
            throw new CannotCancelInProgressReservationException(id);
        }
        if (current != ReservationState.RESERVED) {
            throw new InvalidReservationStateException(current);
        }
        if (actor == CancelledBy.STUDENT && now.isAfter(cancellationDeadline())) {
            throw new CancellationWindowClosedException(id, schedule.startDateTime());
        }
        this.checkpoint = checkpoint.withCancellation(actor, now);
    }

    /** Last instant a student may cancel: block start minus one hour. */
    public LocalDateTime cancellationDeadline() {
        return ReservationTimingCalculator.cancellationDeadline(schedule.startDateTime());
    }

    public void checkIn(LocalDateTime now) {
        this.checkpoint = checkpoint.withCheckIn(now);
    }

    public LocalDateTime checkInOpensAt(Duration tolerance) {
        return ReservationTimingCalculator.checkInOpensAt(schedule.startDateTime(), createdAt, tolerance);
    }

    public LocalDateTime checkInClosesAt(Duration tolerance, Duration minUsage) {
        return ReservationTimingCalculator.checkInClosesAt(
                schedule.startDateTime(), schedule.endDateTime(), createdAt, tolerance, minUsage);
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
        return schedule.date();
    }

    public LocalTime getBlockStart() {
        return schedule.block().start();
    }

    public LocalTime getBlockEnd() {
        return schedule.block().end();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCheckedInAt() {
        return checkpoint.checkedInAt();
    }

    public LocalDateTime getCancelledAt() {
        return checkpoint.cancelledAt();
    }

    public CancelledBy getCancelledBy() {
        return checkpoint.cancelledBy();
    }
}
