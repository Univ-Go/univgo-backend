package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceAvailabilityUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockAvailability;
import com.univgo.backend.reservations.domain.BlockReservations;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationTimingCalculator;
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

@Service
public class GetSpaceAvailabilityService implements GetSpaceAvailabilityUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    public GetSpaceAvailabilityService(
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceScheduleRepositoryPort spaceScheduleRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
    }

    @Override
    public List<BlockAvailability> execute(UUID spaceId, LocalDate date, UUID requestingUserId) {
        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));

        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<TimeBlock> blocks = BlockGenerator.generate(
                spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(spaceId, dayOfWeek), config.blockDuration());

        LocalDateTime now = LocalDateTime.now();

        // Two reads for the whole day instead of two per block: what the student already holds is
        // the same answer for every block, and the overlap check is arithmetic once it is in hand.
        BlockReservations reservations =
                BlockReservations.of(reservationRepositoryPort.findActiveBySpaceAndDate(spaceId, date));
        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findInForceBySpaceId(spaceId));
        List<Reservation> studentsDay = reservationRepositoryPort.findActiveByUserAndDate(requestingUserId, date);

        long heldHereToday =
                studentsDay.stream().filter(reservation -> reservation.getSpaceId().equals(spaceId)).count();
        boolean alreadyReservedToday = heldHereToday >= config.getReservationsPerSpacePerDay();

        return blocks.stream()
                .map(block -> toAvailability(
                        space, date, block, now, config, alreadyReservedToday, reservations, studentsDay, closures))
                .toList();
    }

    /** A person cannot be in two places at once, whichever space the other reservation is for. */
    private static boolean overlaps(List<Reservation> studentsDay, TimeBlock block) {
        return studentsDay.stream()
                .anyMatch(reservation -> reservation.getBlockStart().isBefore(block.end())
                        && reservation.getBlockEnd().isAfter(block.start()));
    }

    private BlockAvailability toAvailability(
            Space space,
            LocalDate date,
            TimeBlock block,
            LocalDateTime now,
            InstitutionConfig config,
            boolean alreadyReservedToday,
            BlockReservations reservations,
            List<Reservation> studentsDay,
            SpaceClosures closures) {
        List<Reservation> active = reservations.of(space.getId(), block);
        long occupied = OccupancyCounter.countOccupiedPlazas(active, closures, now, config);
        int free = (int) Math.max(0, space.getCapacity() - occupied);

        boolean stillBookable =
                ReservationTimingCalculator.isBlockStillBookable(date, block.end(), now, config.minUsage(), config.tolerance());
        boolean overlaps = overlaps(studentsDay, block);
        // A shut block is shown and refused with its own reason rather than left out of the grid:
        // a block that is merely missing reads as "the space closes at ten" (spec §10).
        boolean closed = closures.shut(space.getId(), date, block.start(), block.end());
        boolean offered = stillBookable && free > 0 && !alreadyReservedToday && !overlaps && !closed;

        LocalDateTime blockStartDateTime = LocalDateTime.of(date, block.start());
        LocalDateTime blockEndDateTime = LocalDateTime.of(date, block.end());
        LocalDateTime previewOpens = ReservationTimingCalculator.checkInOpensAt(blockStartDateTime, now, config.tolerance());
        LocalDateTime previewCloses = ReservationTimingCalculator.checkInClosesAt(
                blockStartDateTime, blockEndDateTime, now, config.tolerance(), config.minUsage());

        return new BlockAvailability(
                block,
                space.getCapacity(),
                (int) occupied,
                free,
                offered,
                alreadyReservedToday,
                overlaps,
                closed,
                previewOpens,
                previewCloses);
    }
}
