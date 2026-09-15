package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.CancelReservationUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.CancelledBy;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CancelReservationService implements CancelReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    public CancelReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
    }

    @Override
    public Reservation execute(UUID reservationId, UUID actingUserId, boolean actingAsAdmin) {
        Reservation reservation = reservationRepositoryPort.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        // Hide other students' reservations behind a 404 rather than a 403.
        if (!actingAsAdmin && !reservation.getUserId().equals(actingUserId)) {
            throw new ReservationNotFoundException(reservationId);
        }

        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        CancelledBy actor = actingAsAdmin ? CancelledBy.ADMIN : CancelledBy.STUDENT;
        reservation.cancel(actor, LocalDateTime.now(), config.tolerance(), config.minUsage());

        return reservationRepositoryPort.save(reservation);
    }
}
