package com.univgo.backend.spaces.application.port.in;

import java.util.List;
import java.util.UUID;

public interface UpdateSpaceUsageUseCase {

    void execute(UpdateSpaceUsageCommand command);

    record UpdateSpaceUsageCommand(UUID spaceId, String description, List<String> rules) {
    }
}
