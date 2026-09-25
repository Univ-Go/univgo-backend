package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.in.SetSpaceMaintenanceUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.ClosureCause;
import com.univgo.backend.spaces.domain.ClosurePeriod;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.ClosureReversion;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
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

    public SetSpaceMaintenanceService(
            SpaceRepositoryPort spaceRepositoryPort, SpaceClosureRepositoryPort spaceClosureRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
    }

    @Override
    public void execute(UUID spaceId, boolean underMaintenance, UUID actor) {
        if (!spaceRepositoryPort.existsById(spaceId)) {
            throw new SpaceNotFoundException(spaceId);
        }

        LocalDateTime now = LocalDateTime.now();
        List<SpaceClosure> indefinite = spaceClosureRepositoryPort.findInForceBySpaceId(spaceId).stream()
                .filter(SpaceClosure::isIndefinite)
                .toList();

        if (underMaintenance) {
            // Already shut with no end in sight: switching it on twice would stack closures that
            // then have to be reverted one by one.
            if (indefinite.isEmpty()) {
                spaceClosureRepositoryPort.save(new SpaceClosure(
                        Uuidv7Generator.generate(),
                        spaceId,
                        new ClosurePeriod(now, null),
                        new ClosureCause(ClosureReason.MAINTENANCE, null),
                        actor,
                        now,
                        ClosureReversion.none()));
            }
            return;
        }

        indefinite.forEach(closure -> {
            closure.revert(actor, now);
            spaceClosureRepositoryPort.save(closure);
        });
    }
}
