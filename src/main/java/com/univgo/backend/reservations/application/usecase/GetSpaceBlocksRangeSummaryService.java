package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceBlocksRangeSummaryUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockReservations;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.SpaceDaySummary;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Same per-block math as {@link GetSpaceDayBlocksService}, batched across a date range and
 * reduced to day totals so the frontend's week view isn't firing one request per day.
 */
@Service
public class GetSpaceBlocksRangeSummaryService implements GetSpaceBlocksRangeSummaryUseCase {

    private static final int MAX_RANGE_DAYS = 14;

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;
    private final Clock clock;

    public GetSpaceBlocksRangeSummaryService(
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceScheduleRepositoryPort spaceScheduleRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort,
            Clock clock) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
        this.clock = clock;
    }

    @Override
    public List<SpaceDaySummary> execute(UUID spaceId, LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' must not be before 'from'");
        }
        long rangeDays = ChronoUnit.DAYS.between(from, to) + 1;
        if (rangeDays > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException("Range cannot exceed " + MAX_RANGE_DAYS + " days");
        }

        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findInForceBySpaceId(spaceId));
        LocalDateTime now = LocalDateTime.now(clock);

        // One query for the whole range on each side, grouped in memory — not one pair of queries
        // per day, which is what made this endpoint exist in the first place.
        Map<Integer, List<SpaceSchedule>> schedulesByDayOfWeek = spaceScheduleRepositoryPort
                .findBySpaceId(spaceId)
                .stream()
                .collect(Collectors.groupingBy(SpaceSchedule::getDayOfWeek));
        Map<LocalDate, List<Reservation>> reservationsByDate = reservationRepositoryPort
                .findActiveBySpaceAndDateRange(spaceId, from, to)
                .stream()
                .collect(Collectors.groupingBy(Reservation::getReservationDate));

        List<SpaceDaySummary> summaries = new ArrayList<>((int) rangeDays);
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<TimeBlock> blocks = BlockGenerator.generate(
                    schedulesByDayOfWeek.getOrDefault(date.getDayOfWeek().getValue(), List.of()),
                    config.blockDuration());
            BlockReservations reservations =
                    BlockReservations.of(reservationsByDate.getOrDefault(date, List.of()));

            int totalFree = 0;
            for (TimeBlock block : blocks) {
                long occupied = OccupancyCounter.countOccupiedPlazas(
                        reservations.of(spaceId, block), closures, now, config);
                totalFree += (int) Math.max(0, space.getCapacity() - occupied);
            }

            summaries.add(new SpaceDaySummary(date, space.getCapacity() * blocks.size(), totalFree));
        }
        return summaries;
    }
}
