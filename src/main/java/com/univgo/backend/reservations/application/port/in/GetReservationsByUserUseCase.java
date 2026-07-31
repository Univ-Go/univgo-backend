package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.Reservation;
import java.util.List;
import java.util.UUID;

public interface GetReservationsByUserUseCase {

    List<Reservation> execute(UUID userId);
}
