package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.in.ArchiveSpaceUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceReservationCounterPort;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Retiring a space, which is what "delete" means here. Its reservations are history and
 * {@code reservations.space_id} has no cascade, so a hard delete would either fail on the foreign
 * key or erase what happened.
 *
 * <p>It also opens an indefinite closure, in the same transaction. Without one, an archived space's
 * existing reservations would simply stop being reachable; with one they read as <i>suspended</i>,
 * which spec §12 already defines and which the student's own list already renders. Clearing them is
 * a separate, explicit and irreversible action — {@code cancel-all} — exactly as §12 says.
 *
 * <p>It does not stack a second closure on a space already shut indefinitely, for the same reason
 * the maintenance switch does not: two would have to be reverted one by one.
 */
@Service
public class ArchiveSpaceService implements ArchiveSpaceUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;
    private final SpaceReservationCounterPort spaceReservationCounterPort;
    private final Clock clock;

    public ArchiveSpaceService(
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort,
            SpaceReservationCounterPort spaceReservationCounterPort,
            Clock clock) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
        this.spaceReservationCounterPort = spaceReservationCounterPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public int execute(UUID spaceId, UUID actor) {
        if (!spaceRepositoryPort.existsActiveById(spaceId)) {
            throw new SpaceNotFoundException(spaceId);
        }

        int suspended = spaceReservationCounterPort.countSuspendable(spaceId);
        LocalDateTime now = LocalDateTime.now(clock);

        boolean alreadyShutIndefinitely = spaceClosureRepositoryPort.findInForceBySpaceId(spaceId).stream()
                .anyMatch(SpaceClosure::isIndefinite);

        if (!alreadyShutIndefinitely) {
            spaceClosureRepositoryPort.save(SpaceClosure.builder()
                    .id(Uuidv7Generator.generate())
                    .spaceId(spaceId)
                    .startsAt(now)
                    .reason(ClosureReason.MAINTENANCE)
                    .createdBy(actor)
                    .createdAt(now)
                    .build());
        }

        spaceRepositoryPort.archive(spaceId, actor, now);

        return suspended;
    }
}
