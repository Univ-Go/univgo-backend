package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.CancelledBy;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "reservations")
public class ReservationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "qr_code_data", nullable = false, unique = true)
    private String qrCodeData;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "space_id", nullable = false)
    private UUID spaceId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Convert(converter = CancelledByConverter.class)
    @Column(name = "cancelled_by")
    private CancelledBy cancelledBy;

    protected ReservationJpaEntity() {
    }

    private ReservationJpaEntity(Builder builder) {
        this.id = builder.id;
        this.qrCodeData = builder.qrCodeData;
        this.confirmationCode = builder.confirmationCode;
        this.userId = builder.userId;
        this.spaceId = builder.spaceId;
        this.reservationDate = builder.reservationDate;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
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
        private LocalTime startTime;
        private LocalTime endTime;
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

        public Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
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

        public ReservationJpaEntity build() {
            return new ReservationJpaEntity(this);
        }
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
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
