package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.GuestsNotFoundException;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationGuest;
import com.univgo.backend.reservations.domain.ReservationOverlapException;
import com.univgo.backend.reservations.domain.ReservationStatus;
import com.univgo.backend.reservations.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateReservationService implements CreateReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final SpaceRepositoryPort spaceRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public CreateReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            SpaceRepositoryPort spaceRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Reservation execute(CreateReservationCommand command) {
        if (!spaceRepositoryPort.existsById(command.spaceId())) {
            throw new SpaceNotFoundException(command.spaceId());
        }

        if (reservationRepositoryPort.existsOverlapping(
                command.spaceId(), command.reservationDate(), command.startTime(), command.endTime())) {
            throw new ReservationOverlapException();
        }

        List<ReservationGuest> guests = buildGuests(command.guestIdentifications());

        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                command.userId(),
                command.spaceId(),
                command.reservationDate(),
                command.startTime(),
                command.endTime(),
                ReservationStatus.PENDING,
                now,
                now,
                guests);

        return reservationRepositoryPort.save(reservation);
    }

    private List<ReservationGuest> buildGuests(List<String> guestIdentifications) {
        if (guestIdentifications == null || guestIdentifications.isEmpty()) {
            return List.of();
        }

        List<String> missing = new ArrayList<>();
        List<ReservationGuest> guests = new ArrayList<>();

        for (String identification : guestIdentifications) {
            userRepositoryPort.findByIdentification(identification).ifPresentOrElse(
                    user -> guests.add(new ReservationGuest(
                            UUID.randomUUID(), identification, user.getFirstName() + " " + user.getLastName())),
                    () -> missing.add(identification));
        }

        if (!missing.isEmpty()) {
            throw new GuestsNotFoundException(missing);
        }

        return guests;
    }
}
