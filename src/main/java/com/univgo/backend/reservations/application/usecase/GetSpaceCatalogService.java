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
    public List<SpaceCatalogItem> execute() {
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        return spaceRepositoryPort.findAll().stream()
                .map(space -> new SpaceCatalogItem(
                        space.getId(),
                        space.getName(),
                        space.getCapacity(),
                        space.isUnderMaintenance(),
                        !space.isUnderMaintenance() && hasFreeBlockToday(space, today, now, config)))
                .toList();
    }

    private boolean hasFreeBlockToday(Space space, LocalDate today, LocalDateTime now, InstitutionConfig config) {
        int dayOfWeek = today.getDayOfWeek().getValue();
        List<TimeBlock> blocks = BlockGenerator.generate(
                spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(space.getId(), dayOfWeek), config.blockDuration());

        return blocks.stream().anyMatch(block -> {
            if (!ReservationTimingCalculator.isBlockStillBookable(today, block.end(), now, config.minUsage(), config.tolerance())) {
                return false;
            }
            List<Reservation> active = reservationRepositoryPort.findActiveByBlock(space.getId(), today, block.start(), block.end());
            long occupied = OccupancyCounter.countOccupiedPlazas(active, now, config.tolerance(), config.minUsage());
            return occupied < space.getCapacity();
        });
    }
}
