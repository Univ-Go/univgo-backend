package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.UpdateReservationStatusUseCase;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationNotFoundException;
import com.univgo.backend.reservations.domain.ReservationStatus;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateReservationStatusService implements UpdateReservationStatusUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;

    public UpdateReservationStatusService(ReservationRepositoryPort reservationRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
    }

    @Override
    public Reservation execute(UUID id, ReservationStatus newStatus) {
        Reservation reservation = reservationRepositoryPort.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        reservation.changeStatus(newStatus);

        return reservationRepositoryPort.save(reservation);
    }
}
