package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceBlockDetailUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.OccupancyCounter;
import com.univgo.backend.reservations.domain.OccupantView;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationStatusResolver;
import com.univgo.backend.reservations.domain.SpaceBlockDetail;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.TimeBlock;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetSpaceBlockDetailService implements GetSpaceBlockDetailUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;
    private final Clock clock;

    public GetSpaceBlockDetailService(
            SpaceRepositoryPort spaceRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            InstitutionConfigRepositoryPort institutionConfigRepositoryPort,
            UserRepositoryPort userRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort,
            Clock clock) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
        this.clock = clock;
    }

    @Override
    public SpaceBlockDetail execute(UUID spaceId, LocalDate date, LocalTime blockStart) {
        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));
        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalTime blockEnd = blockStart.plus(config.blockDuration());

        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findInForceBySpaceId(spaceId));
        SpaceClosure closure = closures.covering(spaceId, date, blockStart, blockEnd).orElse(null);

        List<Reservation> active = reservationRepositoryPort.findActiveByBlock(spaceId, date, blockStart, blockEnd);
        long occupied = OccupancyCounter.countOccupiedPlazas(active, closures, now, config);
        int free = (int) Math.max(0, space.getCapacity() - occupied);

        List<OccupantView> roster = active.stream()
                .map(r -> toOccupantView(r, closures, now, config))
                .toList();

        return new SpaceBlockDetail(
                new TimeBlock(blockStart, blockEnd),
                space.getCapacity(),
                (int) occupied,
                free,
                closure != null,
                closure == null ? null : closure.getReason(),
                roster);
    }

    private OccupantView toOccupantView(
            Reservation reservation, SpaceClosures closures, LocalDateTime now, InstitutionConfig config) {
        Optional<User> student = userRepositoryPort.findById(reservation.getUserId());
        String studentName = student.map(user -> user.getFirstName() + " " + user.getLastName()).orElse("Unknown student");
        String document = student.map(User::getIdentification).orElse(null);
        String school = student.map(User::getSchool).orElse(null);

        return new OccupantView(
                studentName,
                document,
                school,
                ReservationStatusResolver.resolve(reservation, closures, now, config).state(),
                reservation.getCheckedInAt());
    }
}
