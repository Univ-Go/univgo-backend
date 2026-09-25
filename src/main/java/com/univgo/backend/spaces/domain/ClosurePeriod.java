package com.univgo.backend.spaces.domain;

import java.time.LocalDateTime;

/** The window a closure covers. {@code endsAt} of {@code null} means "until somebody reverts it". */
public record ClosurePeriod(LocalDateTime startsAt, LocalDateTime endsAt) {

    public ClosurePeriod {
        if (endsAt != null && !endsAt.isAfter(startsAt)) {
            throw new IllegalArgumentException("A closure ends after it starts");
        }
    }
}
