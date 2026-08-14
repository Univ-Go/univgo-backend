package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.DeleteReservationUseCase;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.ReservationNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DeleteReservationService implements DeleteReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;

    public DeleteReservationService(ReservationRepositoryPort reservationRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
    }

    @Override
    public void execute(UUID id) {
        reservationRepositoryPort.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        reservationRepositoryPort.deleteById(id);
    }
}
