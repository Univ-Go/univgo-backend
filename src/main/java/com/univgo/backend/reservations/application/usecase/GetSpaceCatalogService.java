package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceCatalogUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationTimingCalculator;
import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.BlockGenerator;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetSpaceCatalogService implements GetSpaceCatalogUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    public GetSpaceCatalogService(
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
    public List<SpaceCatalogItem> execute(LocalDate date) {
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now();

        return spaceRepositoryPort.findAll().stream()
                .map(space -> new SpaceCatalogItem(
                        space.getId(),
                        space.getName(),
                        space.getLocation(),
                        space.getCategory(),
                        space.getCapacity(),
                        space.isUnderMaintenance(),
                        space.isUnderMaintenance() ? List.of() : freeBlockStarts(space, date, now, config)))
                .toList();
    }

    /**
     * The catalog reads plazas but not the student: whether they already booked here today or clash
     * with another reservation is answered by the availability of a single space, where there is
     * room to explain it. Listing it here would hide the space instead.
     */
    private List<LocalTime> freeBlockStarts(Space space, LocalDate date, LocalDateTime now, InstitutionConfig config) {
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<TimeBlock> blocks = BlockGenerator.generate(
                spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(space.getId(), dayOfWeek), config.blockDuration());

        return blocks.stream().filter(block -> hasRoom(space, date, block, now, config)).map(TimeBlock::start).toList();
    }

    private boolean hasRoom(Space space, LocalDate date, TimeBlock block, LocalDateTime now, InstitutionConfig config) {
        if (!ReservationTimingCalculator.isBlockStillBookable(date, block.end(), now, config.minUsage(), config.tolerance())) {
            return false;
        }
        List<Reservation> active = reservationRepositoryPort.findActiveByBlock(space.getId(), date, block.start(), block.end());
        long occupied = OccupancyCounter.countOccupiedPlazas(active, now, config.tolerance(), config.minUsage());
        return occupied < space.getCapacity();
    }
}
