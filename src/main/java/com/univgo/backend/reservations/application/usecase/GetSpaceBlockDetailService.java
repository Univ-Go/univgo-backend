package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceBlockDetailUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.OccupantView;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.SpaceBlockDetail;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.TimeBlock;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetSpaceBlockDetailService implements GetSpaceBlockDetailUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public GetSpaceBlockDetailService(
            SpaceRepositoryPort spaceRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            UserRepositoryPort userRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public SpaceBlockDetail execute(UUID spaceId, LocalDate date, LocalTime blockStart) {
        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now();
        LocalTime blockEnd = blockStart.plus(config.blockDuration());

        List<Reservation> active = reservationRepositoryPort.findActiveByBlock(spaceId, date, blockStart, blockEnd);
        long occupied = OccupancyCounter.countOccupiedPlazas(active, now, config.tolerance(), config.minUsage());
        int free = (int) Math.max(0, space.getCapacity() - occupied);

        List<OccupantView> roster = active.stream()
                .map(r -> new OccupantView(
                        studentName(r.getUserId()), r.stateAt(now, config.tolerance(), config.minUsage()), r.getCheckedInAt()))
                .toList();

        return new SpaceBlockDetail(new TimeBlock(blockStart, blockEnd), space.getCapacity(), (int) occupied, free, roster);
    }

    private String studentName(UUID userId) {
        return userRepositoryPort.findById(userId)
                .map(user -> user.getFirstName() + " " + user.getLastName())
                .orElse("Unknown student");
    }
}
