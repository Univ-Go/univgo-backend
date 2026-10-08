package com.univgo.backend.spaces.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SpaceScheduleSetTest {

    private static final UUID SPACE_ID = UUID.randomUUID();

    private static SpaceSchedule window(int dayOfWeek, int fromHour, int toHour) {
        return new SpaceSchedule(
                UUID.randomUUID(), SPACE_ID, dayOfWeek, LocalTime.of(fromHour, 0), LocalTime.of(toHour, 0));
    }

    @Test
    void acceptsDisjointWindowsOnTheSameDay() {
        SpaceScheduleSet set = SpaceScheduleSet.of(List.of(window(1, 6, 12), window(1, 14, 22)));

        assertThat(set.getWindows()).hasSize(2);
    }

    @Test
    void acceptsWindowsThatOnlyTouch() {
        SpaceScheduleSet set = SpaceScheduleSet.of(List.of(window(1, 12, 14), window(1, 14, 16)));

        assertThat(set.getWindows()).hasSize(2);
    }

    @Test
    void acceptsIdenticalWindowsOnDifferentDays() {
        SpaceScheduleSet set = SpaceScheduleSet.of(List.of(window(1, 6, 22), window(2, 6, 22)));

        assertThat(set.getWindows()).hasSize(2);
    }

    @Test
    void rejectsAnExactDuplicate() {
        List<SpaceSchedule> windows = List.of(window(1, 6, 22), window(1, 6, 22));

        assertThatThrownBy(() -> SpaceScheduleSet.of(windows))
                .isInstanceOf(OverlappingScheduleException.class);
    }

    @Test
    void rejectsAPartialOverlap() {
        List<SpaceSchedule> windows = List.of(window(3, 6, 12), window(3, 10, 22));

        assertThatThrownBy(() -> SpaceScheduleSet.of(windows))
                .isInstanceOf(OverlappingScheduleException.class)
                .hasMessageContaining("day 3");
    }

    @Test
    void rejectsAWindowContainedInAnother() {
        List<SpaceSchedule> windows = List.of(window(5, 6, 22), window(5, 10, 12));

        assertThatThrownBy(() -> SpaceScheduleSet.of(windows))
                .isInstanceOf(OverlappingScheduleException.class);
    }

    @Test
    void ordersWindowsByWeekdayAndStartTime() {
        SpaceScheduleSet set = SpaceScheduleSet.of(List.of(window(2, 14, 16), window(1, 8, 10), window(2, 6, 8)));

        assertThat(set.getWindows())
                .extracting(SpaceSchedule::getDayOfWeek, SpaceSchedule::getStartTime)
                .containsExactly(
                        tuple(1, LocalTime.of(8, 0)),
                        tuple(2, LocalTime.of(6, 0)),
                        tuple(2, LocalTime.of(14, 0)));
    }

    @Test
    void acceptsAnEmptyWeek() {
        assertThat(SpaceScheduleSet.of(List.of()).getWindows()).isEmpty();
    }
}
