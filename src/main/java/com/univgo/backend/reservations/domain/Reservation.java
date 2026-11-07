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
    private final String confirmationCode;
    private final UUID userId;
    private final UUID spaceId;
    private final LocalDate reservationDate;
    private final LocalTime blockStart;
    private final LocalTime blockEnd;
    private final LocalDateTime createdAt;
    private LocalDateTime checkedInAt;
    private LocalDateTime cancelledAt;
    private CancelledBy cancelledBy;

    private Reservation(Builder builder) {
        if (!builder.blockEnd.isAfter(builder.blockStart)) {
            throw new IllegalArgumentException("blockEnd must be after blockStart");
        }
        this.id = builder.id;
        this.qrCodeData = builder.qrCodeData;
        this.confirmationCode = builder.confirmationCode;
        this.userId = builder.userId;
        this.spaceId = builder.spaceId;
        this.reservationDate = builder.reservationDate;
        this.blockStart = builder.blockStart;
        this.blockEnd = builder.blockEnd;
        this.createdAt = builder.createdAt;
        this.checkedInAt = builder.checkedInAt;
        this.cancelledAt = builder.cancelledAt;
        this.cancelledBy = builder.cancelledBy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private UUID id;
        private String qrCodeData;
        private String confirmationCode;
        private UUID userId;
        private UUID spaceId;
        private LocalDate reservationDate;
        private LocalTime blockStart;
        private LocalTime blockEnd;
        private LocalDateTime createdAt;
        private LocalDateTime checkedInAt;
        private LocalDateTime cancelledAt;
        private CancelledBy cancelledBy;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder qrCodeData(String qrCodeData) {
            this.qrCodeData = qrCodeData;
            return this;
        }

        public Builder confirmationCode(String confirmationCode) {
            this.confirmationCode = confirmationCode;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder spaceId(UUID spaceId) {
            this.spaceId = spaceId;
            return this;
        }

        public Builder reservationDate(LocalDate reservationDate) {
            this.reservationDate = reservationDate;
            return this;
        }

        public Builder blockStart(LocalTime blockStart) {
            this.blockStart = blockStart;
            return this;
        }

        public Builder blockEnd(LocalTime blockEnd) {
            this.blockEnd = blockEnd;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder checkedInAt(LocalDateTime checkedInAt) {
            this.checkedInAt = checkedInAt;
            return this;
        }

        public Builder cancelledAt(LocalDateTime cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public Builder cancelledBy(CancelledBy cancelledBy) {
            this.cancelledBy = cancelledBy;
            return this;
        }

        public Reservation build() {
            return new Reservation(this);
        }
    }

    public ReservationState stateAt(LocalDateTime now, Duration tolerance, Duration minUsage) {
        return ReservationTimingCalculator.stateAt(
                now,
                blockStartDateTime(),
                blockEndDateTime(),
                createdAt,
                new ReservationTimingCalculator.ReservationOutcome(checkedInAt, cancelledAt),
                tolerance,
                minUsage);
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
            throw new CancellationWindowClosedException(id, blockStartDateTime());
        }
        this.cancelledAt = now;
        this.cancelledBy = actor;
    }

    /** Last instant a student may cancel: block start minus one hour. */
    public LocalDateTime cancellationDeadline() {
        return ReservationTimingCalculator.cancellationDeadline(blockStartDateTime());
    }

    public void checkIn(LocalDateTime now) {
        this.checkedInAt = now;
    }

    public LocalDateTime checkInOpensAt() {
        return ReservationTimingCalculator.checkInOpensAt(blockStartDateTime(), createdAt);
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

    public String getConfirmationCode() {
        return confirmationCode;
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
