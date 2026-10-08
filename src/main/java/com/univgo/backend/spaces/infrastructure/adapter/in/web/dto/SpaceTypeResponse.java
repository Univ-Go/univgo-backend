package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceType;
import java.util.UUID;

public record SpaceTypeResponse(UUID id, String name, SpaceCategory category) {

    public static SpaceTypeResponse from(SpaceType type) {
        return new SpaceTypeResponse(type.id(), type.name(), type.category());
    }
}
