package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.in.SetSpaceMaintenanceUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The panel's "out of service" switch, which is a closure with no end date (spec §12). It used to be
 * a boolean on the space, and two mechanisms for "this space is unavailable" had already drifted:
 * the flag hid blocks from students and not from the panel.
 *
 * <p>Turning it off reverts the open-ended closures in force, and only those. A closure with an end
 * date is somebody planning a window, and handing a space back today is not the way to call off next
 * Tuesday.
 */
@Service
public class SetSpaceMaintenanceService implements SetSpaceMaintenanceUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;
    private final Clock clock;

    public SetSpaceMaintenanceService(
            SpaceRepositoryPort spaceRepositoryPort, SpaceClosureRepositoryPort spaceClosureRepositoryPort, Clock clock) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
        this.clock = clock;
    }

    @Override
    public void execute(UUID spaceId, boolean underMaintenance, UUID actor) {
        if (!spaceRepositoryPort.existsActiveById(spaceId)) {
            throw new SpaceNotFoundException(spaceId);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        List<SpaceClosure> indefinite = spaceClosureRepositoryPort.findInForceBySpaceId(spaceId).stream()
                .filter(SpaceClosure::isIndefinite)
                .toList();

        if (underMaintenance) {
            // Already shut with no end in sight: switching it on twice would stack closures that
            // then have to be reverted one by one.
            if (indefinite.isEmpty()) {
                spaceClosureRepositoryPort.save(SpaceClosure.builder()
                        .id(Uuidv7Generator.generate())
                        .spaceId(spaceId)
                        .startsAt(now)
                        .reason(ClosureReason.MAINTENANCE)
                        .createdBy(actor)
                        .createdAt(now)
                        .build());
            }
            return;
        }

        indefinite.forEach(closure -> {
            closure.revert(actor, now);
            spaceClosureRepositoryPort.save(closure);
        });
    }
}
