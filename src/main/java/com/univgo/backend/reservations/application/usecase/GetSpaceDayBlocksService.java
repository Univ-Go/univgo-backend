package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceDayBlocksUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockReservations;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.SpaceBlockSummary;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.BlockGenerator;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Admin visibility, not a booking gate — unlike the student-facing catalog, this ignores maintenance so admins can still see and manage reservations made before a space went under maintenance. */
@Service
public class GetSpaceDayBlocksService implements GetSpaceDayBlocksUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    public GetSpaceDayBlocksService(
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
    public List<SpaceBlockSummary> execute(UUID spaceId, LocalDate date) {
        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now();

        int dayOfWeek = date.getDayOfWeek().getValue();
        BlockReservations reservations =
                BlockReservations.of(reservationRepositoryPort.findActiveBySpaceAndDate(spaceId, date));
        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findInForceBySpaceId(spaceId));

        return BlockGenerator.generate(
                        spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(spaceId, dayOfWeek), config.blockDuration())
                .stream()
                .map(block -> {
                    var active = reservations.of(spaceId, block);
                    long occupied = OccupancyCounter.countOccupiedPlazas(active, closures, now, config);
                    int free = (int) Math.max(0, space.getCapacity() - occupied);
                    // A closed block reports why rather than an aforo nobody can use: a block shown
                    // as available invites counting on places that do not exist.
                    SpaceClosure closure = closures
                            .covering(spaceId, date, block.start(), block.end())
                            .orElse(null);
                    return new SpaceBlockSummary(
                            block,
                            space.getCapacity(),
                            (int) occupied,
                            free,
                            closure != null,
                            closure == null ? null : closure.getReason());
                })
                .toList();
    }
}
