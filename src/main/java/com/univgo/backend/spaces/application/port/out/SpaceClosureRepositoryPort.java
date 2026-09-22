package com.univgo.backend.spaces.application.port.out;

import com.univgo.backend.spaces.domain.SpaceClosure;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpaceClosureRepositoryPort {

    SpaceClosure save(SpaceClosure closure);

    Optional<SpaceClosure> findById(UUID id);

    /** Every closure of a space, reverted ones included: that history is what the panel shows. */
    List<SpaceClosure> findBySpaceId(UUID spaceId);

    List<SpaceClosure> findInForceBySpaceId(UUID spaceId);

    /** One query for the whole catalog, which reads every space at once. */
    List<SpaceClosure> findAllInForce();
}
