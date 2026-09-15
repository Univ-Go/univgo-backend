package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.CancelSpaceReservationsUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.CancelledBy;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationState;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CancelSpaceReservationsService implements CancelSpaceReservationsUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    public CancelSpaceReservationsService(
            ReservationRepositoryPort reservationRepositoryPort, InstitutionConfigRepositoryPort institutionConfigRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
    }

    @Override
    public int execute(UUID spaceId) {
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> activeReservations = reservationRepositoryPort.findActiveBySpaceId(spaceId);

        int cancelled = 0;
        for (Reservation reservation : activeReservations) {
            // Only still-cancellable ("Reservada") reservations — in-progress ones finish on
            // their own, and expired/finished ones are already history.
            if (reservation.stateAt(now, config.tolerance(), config.minUsage()) == ReservationState.RESERVED) {
                reservation.cancel(CancelledBy.ADMIN, now, config.tolerance(), config.minUsage());
                reservationRepositoryPort.save(reservation);
                cancelled++;
            }
        }
        return cancelled;
    }
}
