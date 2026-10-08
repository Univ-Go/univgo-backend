package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.GetAdminSpacesUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceTypeRepositoryPort;
import com.univgo.backend.spaces.domain.AdminSpaceDetail;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import com.univgo.backend.spaces.domain.SpaceScheduleSet;
import com.univgo.backend.spaces.domain.SpaceType;
import com.univgo.backend.spaces.domain.SpaceTypeNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creating a space is one transaction because a space is only useful complete: without its windows
 * it generates no blocks, and without a photograph it reads as unfinished in the catalogue. The
 * wizard therefore sends everything at once and this either produces a space a student can use or
 * produces nothing.
 *
 * <p>The order of the work is the point. Validation first, because it is cheap; then the photographs,
 * which is where almost every real failure lands — a file that is not an image, or one too large —
 * and which fails before a single row is written; then the rows; then the bytes. A transaction is
 * held across the resizing, which is CPU work: deliberate, because the alternative is a second bean
 * whose only purpose is to own a narrower transaction, and one administrator creating one space at a
 * time does not need it.
 */
@Service
public class CreateSpaceService implements CreateSpaceUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceTypeRepositoryPort spaceTypeRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final SpaceImageWriter spaceImageWriter;
    private final GetAdminSpacesUseCase getAdminSpacesUseCase;

    public CreateSpaceService(
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceTypeRepositoryPort spaceTypeRepositoryPort,
            SpaceScheduleRepositoryPort spaceScheduleRepositoryPort,
            SpaceImageWriter spaceImageWriter,
            GetAdminSpacesUseCase getAdminSpacesUseCase) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceTypeRepositoryPort = spaceTypeRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
        this.spaceImageWriter = spaceImageWriter;
        this.getAdminSpacesUseCase = getAdminSpacesUseCase;
    }

    @Override
    @Transactional
    public AdminSpaceDetail execute(CreateSpaceCommand command) {
        if (command.photos().isEmpty()) {
            throw new IllegalArgumentException("A space needs at least one photograph to be published");
        }
        spaceImageWriter.guardTotal(command.photos().size());

        SpaceType type = spaceTypeRepositoryPort
                .findById(command.spaceTypeId())
                .orElseThrow(() -> new SpaceTypeNotFoundException(command.spaceTypeId()));

        UUID spaceId = Uuidv7Generator.generate();
        SpaceScheduleSet windows = toScheduleSet(spaceId, command.schedules());

        spaceRepositoryPort.save(new Space(
                spaceId,
                command.name(),
                command.location(),
                command.capacity(),
                type.id(),
                type.category(),
                command.description(),
                command.rules()));
        spaceScheduleRepositoryPort.replaceAll(spaceId, windows.getWindows());
        spaceImageWriter.store(spaceId, command.photos(), 0, command.actor());

        return getAdminSpacesUseCase.detail(spaceId);
    }

    /**
     * Built as a set before anything is written, so an overlapping week is refused rather than
     * half-created: {@code BlockGenerator} chops each window independently, and two that share a
     * minute would offer the blocks between them twice.
     */
    static SpaceScheduleSet toScheduleSet(UUID spaceId, List<ScheduleWindow> windows) {
        if (windows.isEmpty()) {
            throw new IllegalArgumentException("A space needs at least one opening window to offer blocks");
        }
        return SpaceScheduleSet.of(windows.stream()
                .map(window -> new SpaceSchedule(
                        Uuidv7Generator.generate(),
                        spaceId,
                        window.dayOfWeek(),
                        window.startTime(),
                        window.endTime()))
                .toList());
    }
}
