package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.Reservation;
import java.util.UUID;

public interface CancelReservationUseCase {

    Reservation execute(UUID reservationId, UUID actingUserId, boolean actingAsAdmin);
}
