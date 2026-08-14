package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationStatus;
import java.util.UUID;

public interface UpdateReservationStatusUseCase {

    Reservation execute(UUID id, ReservationStatus newStatus);
}
