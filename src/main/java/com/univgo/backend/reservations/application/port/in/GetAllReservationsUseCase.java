package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.Reservation;
import java.util.List;

public interface GetAllReservationsUseCase {

    List<Reservation> execute();
}
