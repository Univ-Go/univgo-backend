package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetSpaceAvailabilityUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.AvailabilityContext;
import com.univgo.backend.reservations.domain.BlockAvailability;
import com.univgo.backend.reservations.domain.BlockAvailabilityPolicy;
import com.univgo.backend.reservations.domain.BlockReservations;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.StudentDay;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.BlockGenerator;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.Clock;
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
    private final Clock clock;
    private final BlockAvailabilityPolicy availabilityPolicy = new BlockAvailabilityPolicy();

    public GetSpaceAvailabilityService(
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
    public List<BlockAvailability> execute(UUID spaceId, LocalDate date, UUID requestingUserId) {
        Space space = spaceRepositoryPort.findById(spaceId).orElseThrow(() -> new SpaceNotFoundException(spaceId));

        InstitutionConfig config = institutionConfigRepositoryPort.getCurrent();
        int dayOfWeek = date.getDayOfWeek().getValue();
        List<TimeBlock> blocks = BlockGenerator.generate(
                spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(spaceId, dayOfWeek), config.blockDuration());

        LocalDateTime now = LocalDateTime.now(clock);

        // Two reads for the whole day instead of two per block: what the student already holds is
        // the same answer for every block, and the overlap check is arithmetic once it is in hand.
        BlockReservations reservations =
                BlockReservations.of(reservationRepositoryPort.findActiveBySpaceAndDate(spaceId, date));
        SpaceClosures closures = SpaceClosures.of(spaceClosureRepositoryPort.findInForceBySpaceId(spaceId));
        StudentDay studentDay = StudentDay.of(reservationRepositoryPort.findActiveByUserAndDate(requestingUserId, date));

        AvailabilityContext context = new AvailabilityContext(date, now, config, studentDay, reservations, closures);

        return blocks.stream().map(block -> availabilityPolicy.evaluate(space, block, context)).toList();
    }
}
