package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceClosure;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code endsAt} null is a closure with no end date, and {@code revertedAt} is what tells the
 * history from what is in force: a reverted closure is not deleted, it is part of what the panel
 * answers about a space.
 */
public record SpaceClosureResponse(
        UUID id,
        UUID spaceId,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        ClosureReason reason,
        String details,
        UUID createdBy,
        LocalDateTime createdAt,
        LocalDateTime revertedAt,
        UUID revertedBy) {

    public static SpaceClosureResponse from(SpaceClosure closure) {
        return new SpaceClosureResponse(
                closure.getId(),
                closure.getSpaceId(),
                closure.getStartsAt(),
                closure.getEndsAt(),
                closure.getReason(),
                closure.getDetails(),
                closure.getCreatedBy(),
                closure.getCreatedAt(),
                closure.getRevertedAt(),
                closure.getRevertedBy());
    }
}
