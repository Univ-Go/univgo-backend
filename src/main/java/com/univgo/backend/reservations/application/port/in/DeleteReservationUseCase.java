package com.univgo.backend.reservations.application.port.in;

import java.util.UUID;

public interface DeleteReservationUseCase {

    void execute(UUID id);
}
