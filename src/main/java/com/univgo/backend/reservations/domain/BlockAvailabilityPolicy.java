package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Whether one block is offered to one student, and everything they need to see about it. Plain
 * Java on purpose: it takes data the ports have already loaded and answers a question, nothing
 * about where that data came from.
 */
public final class BlockAvailabilityPolicy {

    public BlockAvailability evaluate(Space space, TimeBlock block, AvailabilityContext context) {
        List<Reservation> active = context.reservations().of(space.getId(), block);
        long occupied = OccupancyCounter.countOccupiedPlazas(active, context.closures(), context.now(), context.config());
        int free = (int) Math.max(0, space.getCapacity() - occupied);

        boolean stillBookable = ReservationTimingCalculator.isBlockStillBookable(
                context.date(), block.end(), context.now(), context.config().minUsage(), context.config().tolerance());
        boolean overlaps = context.studentDay().overlaps(block);
        boolean alreadyReservedToday = context.studentDay().hasReachedLimitFor(space.getId(), context.config());

        // A shut block is shown and refused with its own reason rather than left out of the grid:
        // a block that is merely missing reads as "the space closes at ten" (spec §10).
        Optional<SpaceClosure> closure =
                context.closures().covering(space.getId(), context.date(), block.start(), block.end());
        boolean closed = closure.isPresent();
        boolean offered = stillBookable && free > 0 && !alreadyReservedToday && !overlaps && !closed;

        LocalDateTime blockStartDateTime = LocalDateTime.of(context.date(), block.start());
        LocalDateTime blockEndDateTime = LocalDateTime.of(context.date(), block.end());
        LocalDateTime previewOpens =
                ReservationTimingCalculator.checkInOpensAt(blockStartDateTime, context.now(), context.config().tolerance());
        LocalDateTime previewCloses = ReservationTimingCalculator.checkInClosesAt(
                blockStartDateTime, blockEndDateTime, context.now(), context.config().tolerance(), context.config().minUsage());

        return new BlockAvailability(
                block,
                space.getCapacity(),
                (int) occupied,
                free,
                offered,
                alreadyReservedToday,
                overlaps,
                closed,
                closure.map(SpaceClosure::getReason).orElse(null),
                previewOpens,
                previewCloses);
    }
}
