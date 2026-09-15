package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import java.util.UUID;

public record SpaceCatalogResponse(UUID spaceId, String name, int capacity, boolean underMaintenance, boolean hasFreeBlockToday) {

    public static SpaceCatalogResponse from(SpaceCatalogItem item) {
        return new SpaceCatalogResponse(
                item.spaceId(), item.name(), item.capacity(), item.underMaintenance(), item.hasFreeBlockToday());
    }
}
