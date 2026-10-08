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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class CheckInReservationService implements CheckInReservationUseCase {

    private static final Pattern CONFIRMATION_CODE = Pattern.compile("\\d{6}");

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    public CheckInReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
    }

    @Override
    public CheckInResult execute(CheckInCommand command) {
        Optional<Reservation> maybeReservation = findByCode(command.code());
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
        LocalDateTime now = LocalDateTime.now();

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

    // The QR carries the UUID; when it does not scan, the admin types the 6-digit code instead, in the
    // same field. A UUID never matches six digits, so the shape alone says which one it is. The typed
    // code is only unique within a day, and the door only ever checks today's bookings.
    private Optional<Reservation> findByCode(String code) {
        if (CONFIRMATION_CODE.matcher(code).matches()) {
            return reservationRepositoryPort.findActiveByConfirmationCode(LocalDate.now(), code);
        }
        return reservationRepositoryPort.findByQrCodeData(code);
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
