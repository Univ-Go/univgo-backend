package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface CreateReservationUseCase {

    Reservation execute(CreateReservationCommand command);

    record CreateReservationCommand(
            UUID userId,
            UUID spaceId,
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime,
            List<String> guestIdentifications) {
    }
}
