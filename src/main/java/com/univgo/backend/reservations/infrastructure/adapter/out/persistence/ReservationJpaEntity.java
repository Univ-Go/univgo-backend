package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Convert(converter = ReservationStatusConverter.class)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationGuestJpaEntity> guests = new ArrayList<>();

    protected ReservationJpaEntity() {
    }

    public ReservationJpaEntity(
            UUID id,
            String qrCodeData,
            UUID userId,
            UUID spaceId,
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime,
            ReservationStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.qrCodeData = qrCodeData;
        this.userId = userId;
        this.spaceId = spaceId;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void addGuest(ReservationGuestJpaEntity guest) {
        guests.add(guest);
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

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<ReservationGuestJpaEntity> getGuests() {
        return guests;
    }
}
