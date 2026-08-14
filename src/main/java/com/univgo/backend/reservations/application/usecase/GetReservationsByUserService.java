package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetReservationsByUserUseCase;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.Reservation;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetReservationsByUserService implements GetReservationsByUserUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;

    public GetReservationsByUserService(ReservationRepositoryPort reservationRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
    }

    @Override
    public List<Reservation> execute(UUID userId) {
        return reservationRepositoryPort.findByUserId(userId);
    }
}
