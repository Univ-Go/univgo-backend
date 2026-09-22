package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.spaces.domain.ClosureReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * {@code startsAt} null means "from now", and {@code endsAt} null means "until somebody reverts it".
 * Both are deliberate: an incident is registered as it happens, and a space is often shut without
 * knowing yet when it comes back.
 */
public record CreateSpaceClosureRequest(
        LocalDateTime startsAt, LocalDateTime endsAt, @NotNull ClosureReason reason, @Size(max = 500) String details) {
}
