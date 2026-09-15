package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceAvailabilityUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockAvailability;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationTimingCalculator;
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
public class GetSpaceAvailabilityService implements GetSpaceAvailabilityUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    public GetSpaceAvailabilityService(
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceScheduleRepositoryPort spaceScheduleRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
    }

    @Override
    public List<BlockAvailability> execute(UUID spaceId, LocalDate date, UUID requestingUserId) {
        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));

        // Under maintenance: no blocks offered, not an error (spec: "no ofrece bloques").
        if (space.isUnderMaintenance()) {
            return List.of();
        }

        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<TimeBlock> blocks = BlockGenerator.generate(
                spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(spaceId, dayOfWeek), config.blockDuration());

        LocalDateTime now = LocalDateTime.now();
        boolean alreadyReservedToday = reservationRepositoryPort.countActiveByUserSpaceAndDate(requestingUserId, spaceId, date)
                >= config.getReservationsPerSpacePerDay();

        return blocks.stream().map(block -> toAvailability(space, date, block, now, config, alreadyReservedToday, requestingUserId)).toList();
    }

    private BlockAvailability toAvailability(
            Space space,
            LocalDate date,
            TimeBlock block,
            LocalDateTime now,
            InstitutionConfig config,
            boolean alreadyReservedToday,
            UUID requestingUserId) {
        List<Reservation> active = reservationRepositoryPort.findActiveByBlock(space.getId(), date, block.start(), block.end());
        long occupied = OccupancyCounter.countOccupiedPlazas(active, now, config.tolerance(), config.minUsage());
        int free = (int) Math.max(0, space.getCapacity() - occupied);

        boolean stillBookable =
                ReservationTimingCalculator.isBlockStillBookable(date, block.end(), now, config.minUsage(), config.tolerance());
        boolean overlaps = reservationRepositoryPort.existsOverlappingForUser(requestingUserId, date, block.start(), block.end());
        boolean offered = stillBookable && free > 0 && !alreadyReservedToday && !overlaps;

        LocalDateTime blockStartDateTime = LocalDateTime.of(date, block.start());
        LocalDateTime blockEndDateTime = LocalDateTime.of(date, block.end());
        LocalDateTime previewOpens = ReservationTimingCalculator.checkInOpensAt(blockStartDateTime, now, config.tolerance());
        LocalDateTime previewCloses = ReservationTimingCalculator.checkInClosesAt(
                blockStartDateTime, blockEndDateTime, now, config.tolerance(), config.minUsage());

        return new BlockAvailability(
                block, space.getCapacity(), (int) occupied, free, offered, alreadyReservedToday, overlaps, previewOpens, previewCloses);
    }
}
