package com.univgo.backend.reservations.application.port.in;

import java.util.UUID;

public interface CancelSpaceReservationsUseCase {

    /** Cancels every still-reservable ("Reservada") reservation for a space. Returns how many were cancelled. */
    int execute(UUID spaceId);
}
