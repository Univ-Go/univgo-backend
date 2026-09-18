package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.util.List;

public record SpaceBlockDetail(
        TimeBlock block,
        int capacity,
        int occupied,
        int free,
        boolean closed,
        ClosureReason closureReason,
        List<OccupantView> roster) {
}
