package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceCatalogUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockReservations;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.ReservationTimingCalculator;
import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.BlockGenerator;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * The catalog reads the whole day in four queries and does the rest in memory. Asking block by
 * block, as it used to, meant one round trip per block per space — against a database that is not
 * on this machine, that is where the eight seconds went.
 */
@Service
public class GetSpaceCatalogService implements GetSpaceCatalogUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    public GetSpaceCatalogService(
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
    public List<SpaceCatalogItem> execute(LocalDate date) {
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now();

        Map<UUID, List<SpaceSchedule>> schedules = spaceScheduleRepositoryPort
                .findByDayOfWeek(date.getDayOfWeek().getValue())
                .stream()
                .collect(Collectors.groupingBy(SpaceSchedule::getSpaceId));
        BlockReservations reservations = BlockReservations.of(reservationRepositoryPort.findActiveByDate(date));

        // One query for every space's closures, like the schedules and the reservations above: the
        // catalog reads the whole campus, so asking space by space is where the seconds went.
        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findAllInForce());

        return spaceRepositoryPort.findAll().stream()
                .map(space -> new SpaceCatalogItem(
                        space.getId(),
                        space.getName(),
                        space.getLocation(),
                        space.getCategory(),
                        space.getCapacity(),
                        closures.shutAt(space.getId(), now),
                        freeBlockStarts(space, date, now, config, schedules, reservations, closures)))
                .toList();
    }

    /**
     * The catalog reads plazas but not the student: whether they already booked here today or clash
     * with another reservation is answered by the availability of a single space, where there is
     * room to explain it. Listing it here would hide the space instead.
     */
    private List<LocalTime> freeBlockStarts(
            Space space,
            LocalDate date,
            LocalDateTime now,
            InstitutionConfig config,
            Map<UUID, List<SpaceSchedule>> schedules,
            BlockReservations reservations,
            SpaceClosures closures) {
        List<TimeBlock> blocks = BlockGenerator.generate(
                schedules.getOrDefault(space.getId(), List.of()), config.blockDuration());

        return blocks.stream()
                .filter(block -> !closures.shut(space.getId(), date, block.start(), block.end()))
                .filter(block -> hasRoom(space, date, block, now, config, reservations, closures))
                .map(TimeBlock::start)
                .toList();
    }

    private boolean hasRoom(
            Space space,
            LocalDate date,
            TimeBlock block,
            LocalDateTime now,
            InstitutionConfig config,
            BlockReservations reservations,
            SpaceClosures closures) {
        if (!ReservationTimingCalculator.isBlockStillBookable(date, block.end(), now, config.minUsage(), config.tolerance())) {
            return false;
        }
        long occupied =
                OccupancyCounter.countOccupiedPlazas(reservations.of(space.getId(), block), closures, now, config);
        return occupied < space.getCapacity();
    }
}
