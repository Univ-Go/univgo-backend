package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.CancelledBy;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "space_id", nullable = false)
    private UUID spaceId;

    @Embedded
    private ReservationScheduleEmbeddable schedule;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Embedded
    private ReservationCheckpointEmbeddable checkpoint;

    protected ReservationJpaEntity() {
    }

    public ReservationJpaEntity(
            UUID id,
            String qrCodeData,
            UUID userId,
            UUID spaceId,
            ReservationScheduleEmbeddable schedule,
            LocalDateTime createdAt,
            ReservationCheckpointEmbeddable checkpoint) {
        this.id = id;
        this.qrCodeData = qrCodeData;
        this.userId = userId;
        this.spaceId = spaceId;
        this.schedule = schedule;
        this.createdAt = createdAt;
        this.checkpoint = checkpoint;
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
        return schedule.getReservationDate();
    }

    public LocalTime getStartTime() {
        return schedule.getStartTime();
    }

    public LocalTime getEndTime() {
        return schedule.getEndTime();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCheckedInAt() {
        return checkpoint.getCheckedInAt();
    }

    public LocalDateTime getCancelledAt() {
        return checkpoint.getCancelledAt();
    }

    public CancelledBy getCancelledBy() {
        return checkpoint.getCancelledBy();
    }
}
