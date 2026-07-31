package com.univgo.backend.spaces.application.port.out;

import java.util.UUID;

public interface SpaceRepositoryPort {

    boolean existsById(UUID id);
}
