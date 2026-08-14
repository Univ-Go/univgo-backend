package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.Reservation;
import java.util.UUID;

public interface GetReservationByIdUseCase {

    Reservation execute(UUID id);
}
