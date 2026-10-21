package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.spaces.application.port.out.SpaceReservationCounterPort;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Fulfils the {@code spaces} module's count so that archiving a space can say what it affects
 * before it happens. It lives here because the reservations are here; the port lives there because
 * {@code spaces} is the side that asks.
 *
 * <p>"Suspendable" is the same set {@code CancelSpaceReservationsService} acts on — reservations
 * whose computed state is still {@code RESERVED}. One in progress finishes on its own and an
 * expired one is already history, so neither is something a person needs warning about.
 */
@Service
public class SpaceReservationCounter implements SpaceReservationCounterPort {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    public SpaceReservationCounter(
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
    }

    @Override
    public int countSuspendable(UUID spaceId) {
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now();

        return (int) reservationRepositoryPort.findActiveBySpaceId(spaceId).stream()
                .filter(reservation ->
                        reservation.stateAt(now, config.tolerance(), config.minUsage()) == ReservationState.RESERVED)
                .count();
    }
}
