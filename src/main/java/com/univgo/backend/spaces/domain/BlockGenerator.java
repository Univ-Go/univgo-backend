package com.univgo.backend.spaces.domain;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Chops a space's opening-hours windows into the fixed-size blocks students
 * actually reserve. A student picks a block, never a custom start time, so
 * this is the single source of truth for "which blocks exist today."
 *
 * <p>A trailing slice shorter than a full block is dropped, not offered as a
 * short block — the institution's block duration is fixed, not a maximum.
 */
public final class BlockGenerator {

    private BlockGenerator() {
    }

    public static List<TimeBlock> generate(List<SpaceSchedule> schedulesForDay, Duration blockDuration) {
        List<TimeBlock> blocks = new ArrayList<>();
        for (SpaceSchedule schedule : schedulesForDay) {
            LocalTime cursor = schedule.getStartTime();
            while (true) {
                LocalTime blockEnd = cursor.plus(blockDuration);
                if (blockEnd.isAfter(schedule.getEndTime()) || !blockEnd.isAfter(cursor)) {
                    break;
                }
                blocks.add(new TimeBlock(cursor, blockEnd));
                cursor = blockEnd;
            }
        }
        return blocks;
    }
}
