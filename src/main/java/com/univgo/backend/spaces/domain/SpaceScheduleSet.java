package com.univgo.backend.spaces.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A space's whole week of opening windows, validated as a set rather than one window at a time.
 *
 * <p>{@link SpaceSchedule} already rejects a window that ends before it starts, but a window is
 * only legal in the company it keeps: {@link BlockGenerator} walks a day's windows one by one and
 * appends the blocks of each, so two windows that overlap generate the blocks they share once per
 * window. A student would be shown the same block twice and the panel would count its capacity
 * twice.
 *
 * <p>{@code V16} fixed the exact-duplicate case of this with a UNIQUE constraint, after duplicate
 * gym windows did precisely that. A partial overlap — 06:00-12:00 alongside 10:00-22:00 — satisfies
 * that constraint and has the same effect, which is why the rule lives here and not only in the
 * schema. Windows that merely touch are fine: 12:00-14:00 and 14:00-16:00 share no minute.
 */
public final class SpaceScheduleSet {

    private final List<SpaceSchedule> windows;

    private SpaceScheduleSet(List<SpaceSchedule> windows) {
        this.windows = windows;
    }

    /**
     * @throws OverlappingScheduleException if two windows of the same weekday share any minute.
     */
    public static SpaceScheduleSet of(List<SpaceSchedule> windows) {
        List<SpaceSchedule> sorted = new ArrayList<>(windows);
        sorted.sort(Comparator.comparingInt(SpaceSchedule::getDayOfWeek)
                .thenComparing(SpaceSchedule::getStartTime)
                .thenComparing(SpaceSchedule::getEndTime));

        // Sorted by (day, start), so only consecutive pairs can overlap: anything further down the
        // same day starts later still.
        for (int i = 1; i < sorted.size(); i++) {
            SpaceSchedule previous = sorted.get(i - 1);
            SpaceSchedule current = sorted.get(i);
            if (previous.getDayOfWeek() == current.getDayOfWeek()
                    && current.getStartTime().isBefore(previous.getEndTime())) {
                throw new OverlappingScheduleException(
                        current.getDayOfWeek(),
                        previous.getStartTime(),
                        previous.getEndTime(),
                        current.getStartTime(),
                        current.getEndTime());
            }
        }
        return new SpaceScheduleSet(List.copyOf(sorted));
    }

    /** Ordered by weekday and then by start time, which is the order a week is read in. */
    public List<SpaceSchedule> getWindows() {
        return windows;
    }
}
