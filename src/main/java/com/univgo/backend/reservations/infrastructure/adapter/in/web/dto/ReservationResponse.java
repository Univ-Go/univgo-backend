package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
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
        List<GuestResponse> guests) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getQrCodeData(),
                reservation.getUserId(),
                reservation.getSpaceId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                reservation.getGuests().stream().map(GuestResponse::from).toList());
    }
}
