package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.CheckInReservationUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.CheckInResult;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CheckInReservationService implements CheckInReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;
    private final Clock clock;

    public CheckInReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort,
            Clock clock) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
        this.clock = clock;
    }

    @Override
    public CheckInResult execute(CheckInCommand command) {
        Optional<Reservation> maybeReservation = reservationRepositoryPort.findByQrCodeData(command.code());
        if (maybeReservation.isEmpty()) {
            return CheckInResult.notExists();
        }

        Reservation reservation = maybeReservation.get();
        if (reservation.getCancelledAt() != null) {
            return CheckInResult.notExists();
        }

        if (!reservation.getSpaceId().equals(command.spaceId())) {
            return CheckInResult.otherBlock(reservation.getBlockStart(), reservation.getBlockEnd());
        }

        if (command.expectedBlockStart() != null
                && command.expectedBlockEnd() != null
                && (!reservation.getBlockStart().equals(command.expectedBlockStart())
                        || !reservation.getBlockEnd().equals(command.expectedBlockEnd()))) {
            return CheckInResult.otherBlock(reservation.getBlockStart(), reservation.getBlockEnd());
        }

        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now(clock);

        // A closed door lets nobody in, and it is not the student's fault either: the reservation is
        // suspended, not expired, and it comes back if the closure is reverted (spec §12).
        SpaceClosures closures =
                SpaceClosures.of(spaceClosureRepositoryPort.findInForceBySpaceId(reservation.getSpaceId()));
        if (closures.shut(
                reservation.getSpaceId(),
                reservation.getReservationDate(),
                reservation.getBlockStart(),
                reservation.getBlockEnd())) {
            return CheckInResult.spaceClosed();
        }

        ReservationState state = reservation.stateAt(now, config.tolerance(), config.minUsage());

        return switch (state) {
            case IN_PROGRESS, FINISHED -> CheckInResult.alreadyUsed(studentName(reservation), reservation.getCheckedInAt());
            case EXPIRED -> CheckInResult.expiredAlready(reservation.checkInClosesAt(config.tolerance(), config.minUsage()));
            case CANCELLED -> CheckInResult.notExists();
            // Unreachable: the closure check above already answered, and it is the only thing that
            // suspends. Stated rather than defaulted, so a seventh state cannot slip through here.
            case SUSPENDED -> CheckInResult.spaceClosed();
            case RESERVED -> checkInIfWindowIsOpen(reservation, now, config);
        };
    }

    private CheckInResult checkInIfWindowIsOpen(Reservation reservation, LocalDateTime now, InstitutionConfig config) {
        LocalDateTime opensAt = reservation.checkInOpensAt(config.tolerance());
        if (now.isBefore(opensAt)) {
            return CheckInResult.tooEarly(opensAt);
        }

        reservation.checkIn(now);
        reservationRepositoryPort.save(reservation);

        LocalDateTime blockEnd = LocalDateTime.of(reservation.getReservationDate(), reservation.getBlockEnd());
        return CheckInResult.valid(studentName(reservation), blockEnd, now);
    }

    private String studentName(Reservation reservation) {
        return userRepositoryPort.findById(reservation.getUserId())
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse("Unknown student");
    }
}
