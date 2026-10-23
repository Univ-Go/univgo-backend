package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase.ScheduleWindow;
import com.univgo.backend.spaces.application.port.in.ReplaceSpaceSchedulesUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import com.univgo.backend.spaces.domain.SpaceScheduleSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Replaces a space's week whole, which is the contract {@code V17} already stated. Transactional
 * because the delete and the inserts are one change: halfway through, a space has lost windows it
 * should still have, and the catalogue would show it closed.
 */
@Service
public class ReplaceSpaceSchedulesService implements ReplaceSpaceSchedulesUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;

    public ReplaceSpaceSchedulesService(
            SpaceRepositoryPort spaceRepositoryPort, SpaceScheduleRepositoryPort spaceScheduleRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
    }

    @Override
    @Transactional
    public List<SpaceSchedule> execute(UUID spaceId, List<ScheduleWindow> windows) {
        if (spaceRepositoryPort.findById(spaceId).isEmpty()) {
            throw new SpaceNotFoundException(spaceId);
        }

        // Validated before the delete, so a refused week leaves the old one standing.
        SpaceScheduleSet validated = CreateSpaceService.toScheduleSet(spaceId, windows);
        spaceScheduleRepositoryPort.replaceAll(spaceId, validated.getWindows());

        return validated.getWindows();
    }
}
