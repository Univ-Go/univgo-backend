package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.GetAdminSpacesUseCase;
import com.univgo.backend.spaces.application.port.in.GetSpaceImagesUseCase;
import com.univgo.backend.spaces.application.port.out.AdminSpaceViewRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.AdminSpaceDetail;
import com.univgo.backend.spaces.domain.AdminSpaceSummary;
import com.univgo.backend.spaces.domain.AdminSpaceView;
import com.univgo.backend.spaces.domain.SpaceImageView;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The panel's read of the space catalogue. It composes three reads rather than one join because the
 * windows and the photographs are already batched by space for the student catalogue, and reusing
 * them keeps one definition of "this space's week" instead of two that can disagree.
 */
@Service
public class GetAdminSpacesService implements GetAdminSpacesUseCase {

    private final AdminSpaceViewRepositoryPort adminSpaceViewRepositoryPort;
    private final SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;
    private final GetSpaceImagesUseCase getSpaceImagesUseCase;

    public GetAdminSpacesService(
            AdminSpaceViewRepositoryPort adminSpaceViewRepositoryPort,
            SpaceScheduleRepositoryPort spaceScheduleRepositoryPort,
            GetSpaceImagesUseCase getSpaceImagesUseCase) {
        this.adminSpaceViewRepositoryPort = adminSpaceViewRepositoryPort;
        this.spaceScheduleRepositoryPort = spaceScheduleRepositoryPort;
        this.getSpaceImagesUseCase = getSpaceImagesUseCase;
    }

    @Override
    public List<AdminSpaceSummary> list(boolean includeArchived) {
        Map<UUID, List<SpaceSchedule>> schedules = spaceScheduleRepositoryPort.findAllBySpaceIds();
        Map<UUID, List<SpaceImageView>> images = getSpaceImagesUseCase.allBySpace();

        return adminSpaceViewRepositoryPort.findAll(includeArchived).stream()
                .map(space -> toSummary(space, schedules, images))
                .toList();
    }

    @Override
    public AdminSpaceDetail detail(UUID spaceId) {
        AdminSpaceView space = adminSpaceViewRepositoryPort
                .findById(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException(spaceId));

        return new AdminSpaceDetail(
                space,
                spaceScheduleRepositoryPort.findBySpaceId(spaceId),
                getSpaceImagesUseCase.ofSpace(spaceId));
    }

    private static AdminSpaceSummary toSummary(
            AdminSpaceView space,
            Map<UUID, List<SpaceSchedule>> schedules,
            Map<UUID, List<SpaceImageView>> images) {
        List<SpaceImageView> spaceImages = images.getOrDefault(space.spaceId(), List.of());

        return new AdminSpaceSummary(
                space,
                schedules.getOrDefault(space.spaceId(), List.of()).size(),
                spaceImages.size(),
                spaceImages.isEmpty() ? null : spaceImages.getFirst().smallestUrl());
    }
}
