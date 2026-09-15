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
import com.univgo.backend.reservations.domain.ReservationTimingCalculator;
import com.univgo.backend.reservations.domain.SpaceAlreadyReservedTodayException;
import com.univgo.backend.reservations.domain.SpaceUnderMaintenanceException;
import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.BlockGenerator;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CreateReservationService implements CreateReservationUseCase {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    public CreateReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceScheduleRepositoryPort spaceScheduleRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
    }

    @Override
    public Reservation execute(CreateReservationCommand command) {
        UUID spaceId = command.spaceId();
        LocalDate date = command.reservationDate();

        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));
        if (space.isUnderMaintenance()) {
            throw new SpaceUnderMaintenanceException(spaceId);
        }

        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();

        TimeBlock block = findRequestedBlock(command, config);

        LocalDateTime now = LocalDateTime.now();
        if (!ReservationTimingCalculator.isBlockStillBookable(date, block.end(), now, config.minUsage(), config.tolerance())) {
            throw new BlockNoLongerBookableException(spaceId, date, block.start(), block.end());
        }

        if (reservationRepositoryPort.countActiveByUserSpaceAndDate(command.userId(), spaceId, date)
                >= config.getReservationsPerSpacePerDay()) {
            throw new SpaceAlreadyReservedTodayException(spaceId, date);
        }

        if (reservationRepositoryPort.existsOverlappingForUser(command.userId(), date, block.start(), block.end())) {
            throw new ReservationOverlapException();
        }

        long occupied = countOccupiedPlazas(spaceId, date, block, now, config);
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
            UUID spaceId, LocalDate date, TimeBlock block, LocalDateTime now, InstitutionConfig config) {
        List<Reservation> active = reservationRepositoryPort.findActiveByBlock(spaceId, date, block.start(), block.end());
        return OccupancyCounter.countOccupiedPlazas(active, now, config.tolerance(), config.minUsage());
    }
}
