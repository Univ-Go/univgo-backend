package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.TimeBlock;

public record SpaceBlockSummary(TimeBlock block, int capacity, int occupied, int free) {
}
