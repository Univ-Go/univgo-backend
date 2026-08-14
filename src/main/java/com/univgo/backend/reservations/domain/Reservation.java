package com.univgo.backend.reservations.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class Reservation {

    private final UUID id;
    private final String qrCodeData;
    private final UUID userId;
    private final UUID spaceId;
    private final LocalDate reservationDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private ReservationStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<ReservationGuest> guests;

    public Reservation(
            UUID id,
            String qrCodeData,
            UUID userId,
            UUID spaceId,
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime,
            ReservationStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<ReservationGuest> guests) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
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
        this.guests = guests;
    }

    public void changeStatus(ReservationStatus newStatus) {
        if (status == ReservationStatus.CANCELLED_BY_ADMIN
                || status == ReservationStatus.CANCELLED_BY_USER
                || status == ReservationStatus.COMPLETED
                || status == ReservationStatus.EXPIRED) {
            throw new InvalidReservationStateException(status, newStatus);
        }
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
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

    public List<ReservationGuest> getGuests() {
        return guests;
    }
}
