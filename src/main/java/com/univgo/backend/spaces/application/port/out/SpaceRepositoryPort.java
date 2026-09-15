package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.Space;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpaceRepositoryPort {

    boolean existsById(UUID id);

    Optional<Space> findById(UUID id);

    List<Space> findAll();

    Space save(Space space);
}
