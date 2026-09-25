package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceCatalogUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceDetailUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockReservations;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.ReservationTimingCalculator;
import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.BlockGenerator;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.Clock;
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
 *
 * <p>It answers for one space as well as for the campus: reading a single space asks exactly the
 * same questions of exactly the same six collaborators, so a separate service would be this class
 * again with one call swapped.
 */
@Service
public class GetSpaceCatalogService implements GetSpaceCatalogUseCase, GetSpaceDetailUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;
    private final SpaceImageRepositoryPort spaceImageRepositoryPort;
    private final Clock clock;

    public GetSpaceCatalogService(
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceScheduleRepositoryPort spaceScheduleRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort,
            SpaceImageRepositoryPort spaceImageRepositoryPort,
            Clock clock) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
        this.spaceImageRepositoryPort = spaceImageRepositoryPort;
        this.clock = clock;
    }

    @Override
    public List<SpaceCatalogItem> execute(LocalDate date) {
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now(clock);

        Map<UUID, List<SpaceSchedule>> schedules = spaceScheduleRepositoryPort
                .findByDayOfWeek(date.getDayOfWeek().getValue())
                .stream()
                .collect(Collectors.groupingBy(SpaceSchedule::getSpaceId));
        BlockReservations reservations = BlockReservations.of(reservationRepositoryPort.findActiveByDate(date));

        // One query for every space's closures, like the schedules and the reservations above: the
        // catalog reads the whole campus, so asking space by space is where the seconds went.
        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findAllInForce());
        Map<UUID, List<String>> images = spaceImageRepositoryPort.findAllImageUrls();

        CatalogQueryContext context = new CatalogQueryContext(now, config, schedules, reservations, closures, images);

        return spaceRepositoryPort.findAll().stream()
                .map(space -> toCatalogItem(space, date, context))
                .toList();
    }

    @Override
    public SpaceCatalogItem execute(UUID spaceId, LocalDate date) {
        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));

        Map<UUID, List<SpaceSchedule>> schedules = spaceScheduleRepositoryPort
                .findByDayOfWeek(date.getDayOfWeek().getValue())
                .stream()
                .collect(Collectors.groupingBy(SpaceSchedule::getSpaceId));

        CatalogQueryContext context = new CatalogQueryContext(
                LocalDateTime.now(clock),
                institutionConfigRepositoryPort.getCurrent(),
                schedules,
                BlockReservations.of(reservationRepositoryPort.findActiveByDate(date)),
                SpaceClosures.of(spaceClosureRepositoryPort.findAllInForce()),
                spaceImageRepositoryPort.findAllImageUrls());

        return toCatalogItem(space, date, context);
    }

    private SpaceCatalogItem toCatalogItem(Space space, LocalDate date, CatalogQueryContext context) {
        List<TimeBlock> blocks = BlockGenerator.generate(
                context.schedules().getOrDefault(space.getId(), List.of()), context.config().blockDuration());

        // Three different silences, told apart here because the catalog is where somebody decides
        // whether to walk over: a space with no hours that day, one that is shut, and a full one.
        boolean opensOnDate = !blocks.isEmpty();
        boolean closedOnDate = opensOnDate
                && blocks.stream()
                        .allMatch(block -> context.closures().shut(space.getId(), date, block.start(), block.end()));

        return new SpaceCatalogItem(
                space.getId(),
                space.getName(),
                space.getLocation(),
                space.getCategory(),
                space.getCapacity(),
                context.closures().shutAt(space.getId(), context.now()),
                opensOnDate,
                closedOnDate,
                freeBlockStarts(blocks, space, date, context),
                context.images().getOrDefault(space.getId(), List.of()),
                space.getDescription(),
                space.getRules());
    }

    /**
     * The catalog reads plazas but not the student: whether they already booked here today or clash
     * with another reservation is answered by the availability of a single space, where there is
     * room to explain it. Listing it here would hide the space instead.
     */
    private List<LocalTime> freeBlockStarts(List<TimeBlock> blocks, Space space, LocalDate date, CatalogQueryContext context) {
        return blocks.stream()
                .filter(block -> !context.closures().shut(space.getId(), date, block.start(), block.end()))
                .filter(block -> hasRoom(space, date, block, context))
                .map(TimeBlock::start)
                .toList();
    }

    private boolean hasRoom(Space space, LocalDate date, TimeBlock block, CatalogQueryContext context) {
        if (!ReservationTimingCalculator.isBlockStillBookable(
                date, block.end(), context.now(), context.config().minUsage(), context.config().tolerance())) {
            return false;
        }
        long occupied = OccupancyCounter.countOccupiedPlazas(
                context.reservations().of(space.getId(), block), context.closures(), context.now(), context.config());
        return occupied < space.getCapacity();
    }

    /** Bundles the per-request lookups that stay the same across every space/block evaluated in one call. */
    private record CatalogQueryContext(
            LocalDateTime now,
            InstitutionConfig config,
            Map<UUID, List<SpaceSchedule>> schedules,
            BlockReservations reservations,
            SpaceClosures closures,
            Map<UUID, List<String>> images) {}
}
