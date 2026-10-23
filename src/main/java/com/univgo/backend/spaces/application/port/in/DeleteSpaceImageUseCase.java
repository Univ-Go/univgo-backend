package com.univgo.backend.spaces.application.port.in;

import java.util.UUID;

public interface DeleteSpaceImageUseCase {

    void execute(UUID spaceId, UUID imageId);
}
