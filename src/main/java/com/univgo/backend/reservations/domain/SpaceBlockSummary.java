package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.TimeBlock;

/**
 * {@code closureReason} is set exactly when the block is closed: the panel says why a block is out
 * rather than showing an occupancy that stopped meaning anything (spec §12).
 */
public record SpaceBlockSummary(
        TimeBlock block, int capacity, int occupied, int free, boolean closed, ClosureReason closureReason) {
}
