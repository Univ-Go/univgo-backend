package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetAllReservationsUseCase;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.Reservation;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetAllReservationsService implements GetAllReservationsUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;

    public GetAllReservationsService(ReservationRepositoryPort reservationRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
    }

    @Override
    public List<Reservation> execute() {
        return reservationRepositoryPort.findAll();
    }
}
