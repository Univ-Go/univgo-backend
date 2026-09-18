package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.BlockAvailability;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BlockAvailabilityResponse(
        LocalTime start,
        LocalTime end,
        int capacity,
        int occupied,
        int free,
        boolean offered,
        boolean alreadyReservedByUserToday,
        boolean overlapsUserReservation,
        boolean closed,
        LocalDateTime previewCheckInOpensAt,
        LocalDateTime previewCheckInClosesAt) {

    public static BlockAvailabilityResponse from(BlockAvailability availability) {
        return new BlockAvailabilityResponse(
                availability.block().start(),
                availability.block().end(),
                availability.capacity(),
                availability.occupied(),
                availability.free(),
                availability.offered(),
                availability.alreadyReservedByUserToday(),
                availability.overlapsUserReservation(),
                availability.closed(),
                availability.previewCheckInOpensAt(),
                availability.previewCheckInClosesAt());
    }
}
