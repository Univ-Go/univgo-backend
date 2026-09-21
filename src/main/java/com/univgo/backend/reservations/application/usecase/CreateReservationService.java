package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockCapacityFullException;
import com.univgo.backend.reservations.domain.BlockNoLongerBookableException;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationOverlapException;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.reservations.domain.ReservationTimingCalculator;
import com.univgo.backend.reservations.domain.SpaceAlreadyReservedTodayException;
import com.univgo.backend.reservations.domain.ReservationStatusResolver;
import com.univgo.backend.reservations.domain.SpaceClosedException;
import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.BlockGenerator;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateReservationService implements CreateReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    public CreateReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceScheduleRepositoryPort spaceScheduleRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
    }

    /**
     the count, the check and the insert have to sit
     inside the same transaction for the lock to still be held when the row lands.
     for another user the differences are ms
     */
    @Override
    @Transactional
    public Reservation execute(CreateReservationCommand command) {
        UUID spaceId = command.spaceId();
        LocalDate date = command.reservationDate();

        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));

        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();

        TimeBlock block = findRequestedBlock(command, config);

        LocalDateTime now = LocalDateTime.now();
        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findInForceBySpaceId(spaceId));

        if (closures.shut(spaceId, date, block.start(), block.end())) {
            throw new SpaceClosedException(spaceId);
        }

        if (!ReservationTimingCalculator.isBlockStillBookable(date, block.end(), now, config.minUsage(), config.tolerance())) {
            throw new BlockNoLongerBookableException(spaceId, date, block.start(), block.end());
        }

        // The day's booking is counted over what still stands: one the university closed gave it
        // back, so it must not be the reason the student cannot book again (spec §12).
        long heldHereToday = reservationRepositoryPort.findActiveByUserAndDate(command.userId(), date).stream()
                .filter(reservation -> reservation.getSpaceId().equals(spaceId))
                .map(reservation -> ReservationStatusResolver.resolve(reservation, closures, now, config).state())
                .filter(state -> state != ReservationState.SUSPENDED && state != ReservationState.CANCELLED)
                .count();

        if (heldHereToday >= config.getReservationsPerSpacePerDay()) {
            throw new SpaceAlreadyReservedTodayException(spaceId, date);
        }

        if (reservationRepositoryPort.existsOverlappingForUser(command.userId(), date, block.start(), block.end())) {
            throw new ReservationOverlapException();
        }

        // Aforo is the one gate decided against a figure that other bookings move. Taken here and
        // not earlier, so the cheap refusals above never make anybody queue; held to commit, so the
        // last plaza cannot be counted free by two requests at once and sold twice.
        reservationRepositoryPort.lockBlockForBooking(spaceId, date, block.start());

        long occupied = countOccupiedPlazas(spaceId, date, block, now, config, closures);
        if (occupied >= space.getCapacity()) {
            throw new BlockCapacityFullException(spaceId, date, block.start(), block.end());
        }

        Reservation reservation = new Reservation(
                Uuidv7Generator.generate(),
                UUID.randomUUID().toString(),
                command.userId(),
                spaceId,
                date,
                block.start(),
                block.end(),
                now,
                null,
                null,
                null);

        return reservationRepositoryPort.save(reservation);
    }

    private TimeBlock findRequestedBlock(CreateReservationCommand command, InstitutionConfig config) {
        int dayOfWeek = command.reservationDate().getDayOfWeek().getValue();
        List<TimeBlock> blocks = BlockGenerator.generate(
                spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(command.spaceId(), dayOfWeek), config.blockDuration());

        return blocks.stream()
                .filter(b -> b.start().equals(command.requestedStartTime()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Space " + command.spaceId() + " has no block starting at " + command.requestedStartTime()
                                + " on " + command.reservationDate()));
    }

    private long countOccupiedPlazas(
            UUID spaceId,
            LocalDate date,
            TimeBlock block,
            LocalDateTime now,
            InstitutionConfig config,
            SpaceClosures closures) {
        List<Reservation> active = reservationRepositoryPort.findActiveByBlock(spaceId, date, block.start(), block.end());
        return OccupancyCounter.countOccupiedPlazas(active, closures, now, config);
    }
}
