package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetReservationByIdUseCase;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetReservationByIdService implements GetReservationByIdUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;

    public GetReservationByIdService(ReservationRepositoryPort reservationRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
    }

    @Override
    public Reservation execute(UUID id) {
        return reservationRepositoryPort.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }
}
