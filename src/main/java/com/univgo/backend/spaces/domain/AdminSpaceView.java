package com.univgo.backend.spaces.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A space as the panel reads it, which is not what a student reads. The catalogue answers "can I
 * book here on this date" and so computes blocks, capacity and closures; the panel answers "what is
 * this space configured as" and needs the type it points at and whether it has been retired —
 * neither of which the catalogue exposes.
 *
 * <p>Separate from {@link Space} for the same reason {@code SpaceCatalogItem} is: a read model may
 * carry derived facts, and {@code archivedAt} deliberately never reaches the domain object.
 */
public record AdminSpaceView(
        UUID spaceId,
        String name,
        String location,
        UUID spaceTypeId,
        String spaceTypeName,
        SpaceCategory category,
        int capacity,
        String description,
        List<String> rules,
        LocalDateTime archivedAt) {

    public AdminSpaceView {
        rules = List.copyOf(rules);
    }

    public boolean archived() {
        return archivedAt != null;
    }
}
