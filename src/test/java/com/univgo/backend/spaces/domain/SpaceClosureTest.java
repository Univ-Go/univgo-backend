package com.univgo.backend.spaces.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SpaceClosureTest {

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID ADMIN_ID = UUID.randomUUID();
    private static final LocalDate DAY = LocalDate.of(2026, 9, 17);
    private static final LocalTime BLOCK_START = LocalTime.of(14, 0);
    private static final LocalTime BLOCK_END = LocalTime.of(16, 0);

    @Test
    void coversABlockItOverlapsAtAll() {
        SpaceClosure lateAfternoon =
                closure(LocalDateTime.of(DAY, LocalTime.of(15, 0)), LocalDateTime.of(DAY, LocalTime.of(20, 0)));

        // Half a block is not a block anybody can use: offering it would promise a room that shuts
        // halfway through.
        assertThat(lateAfternoon.coversBlock(DAY, BLOCK_START, BLOCK_END)).isTrue();
    }

    @Test
    void doesNotCoverABlockThatEndsAsItStarts() {
        SpaceClosure fromFour =
                closure(LocalDateTime.of(DAY, LocalTime.of(16, 0)), LocalDateTime.of(DAY, LocalTime.of(20, 0)));

        assertThat(fromFour.coversBlock(DAY, BLOCK_START, BLOCK_END)).isFalse();
    }

    @Test
    void doesNotCoverABlockOnAnotherDay() {
        SpaceClosure thatAfternoon =
                closure(LocalDateTime.of(DAY, BLOCK_START), LocalDateTime.of(DAY, LocalTime.of(20, 0)));

        assertThat(thatAfternoon.coversBlock(DAY.plusDays(1), BLOCK_START, BLOCK_END)).isFalse();
    }

    @Test
    void withNoEndDateItCoversEverythingFromItsStart() {
        SpaceClosure indefinite = closure(LocalDateTime.of(DAY, LocalTime.of(9, 0)), null);

        assertThat(indefinite.isIndefinite()).isTrue();
        assertThat(indefinite.coversBlock(DAY, BLOCK_START, BLOCK_END)).isTrue();
        assertThat(indefinite.coversBlock(DAY.plusYears(1), BLOCK_START, BLOCK_END)).isTrue();
        assertThat(indefinite.coversBlock(DAY.minusDays(1), BLOCK_START, BLOCK_END)).isFalse();
    }

    @Test
    void aRevertedClosureCoversNothing() {
        SpaceClosure indefinite = closure(LocalDateTime.of(DAY, LocalTime.of(9, 0)), null);

        indefinite.revert(ADMIN_ID, LocalDateTime.of(DAY, LocalTime.of(12, 0)));

        assertThat(indefinite.isInForce()).isFalse();
        assertThat(indefinite.coversBlock(DAY, BLOCK_START, BLOCK_END)).isFalse();
        assertThat(indefinite.getRevertedBy()).isEqualTo(ADMIN_ID);
    }

    @Test
    void cannotBeRevertedTwice() {
        SpaceClosure indefinite = closure(LocalDateTime.of(DAY, LocalTime.of(9, 0)), null);
        indefinite.revert(ADMIN_ID, LocalDateTime.of(DAY, LocalTime.of(12, 0)));

        assertThatThrownBy(() -> indefinite.revert(ADMIN_ID, LocalDateTime.of(DAY, LocalTime.of(13, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEndBeforeItStarts() {
        assertThatThrownBy(() -> closure(LocalDateTime.of(DAY, LocalTime.of(12, 0)), LocalDateTime.of(DAY, LocalTime.of(9, 0))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void coversAnInstantInsideItsWindowAndNotTheOneItEndsOn() {
        SpaceClosure morning =
                closure(LocalDateTime.of(DAY, LocalTime.of(9, 0)), LocalDateTime.of(DAY, LocalTime.of(12, 0)));

        assertThat(morning.coversInstant(LocalDateTime.of(DAY, LocalTime.of(9, 0)))).isTrue();
        assertThat(morning.coversInstant(LocalDateTime.of(DAY, LocalTime.of(11, 59)))).isTrue();
        assertThat(morning.coversInstant(LocalDateTime.of(DAY, LocalTime.of(12, 0)))).isFalse();
    }

    private static SpaceClosure closure(LocalDateTime startsAt, LocalDateTime endsAt) {
        return new SpaceClosure(
                UUID.randomUUID(),
                SPACE_ID,
                startsAt,
                endsAt,
                ClosureReason.MAINTENANCE,
                null,
                ADMIN_ID,
                LocalDateTime.now(),
                null,
                null);
    }
}
