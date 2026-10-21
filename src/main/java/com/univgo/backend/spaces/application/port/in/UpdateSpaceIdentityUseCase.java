package com.univgo.backend.spaces.application.port.in;

import java.util.UUID;

public interface UpdateSpaceIdentityUseCase {

    void execute(UpdateSpaceIdentityCommand command);

    record UpdateSpaceIdentityCommand(
            UUID spaceId, String name, String location, UUID spaceTypeId, int capacity) {
    }
}
