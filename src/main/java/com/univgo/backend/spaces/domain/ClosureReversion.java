package com.univgo.backend.spaces.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/** Whether, when and by whom a closure was reverted. */
public record ClosureReversion(LocalDateTime revertedAt, UUID revertedBy) {

    public static ClosureReversion none() {
        return new ClosureReversion(null, null);
    }

    public boolean isReverted() {
        return revertedAt != null;
    }
}
