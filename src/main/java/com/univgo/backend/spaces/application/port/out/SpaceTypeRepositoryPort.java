package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.SpaceType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpaceTypeRepositoryPort {

    /** Ordered by name: it is read to populate a picker. */
    List<SpaceType> findAll();

    Optional<SpaceType> findById(UUID id);
}
