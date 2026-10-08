package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.AdminSpaceView;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminSpaceViewRepositoryPort {

    /** Ordered by name. Archived spaces are left out unless asked for. */
    List<AdminSpaceView> findAll(boolean includeArchived);

    /** Answers for an archived space: the panel is where one is restored from. */
    Optional<AdminSpaceView> findById(UUID spaceId);
}
