package com.univgo.backend.reservations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/**
 * Every assertion here is lifted straight from the spec's own worked examples
 * for the 14:00-16:00 block (tolerance 15min, min usage 75min), so a wrong
 * formula fails loudly against numbers the university already agreed to.
 */
class ReservationTimingCalculatorTest {

    private static final Duration TOLERANCE = Duration.ofMinutes(15);
    private static final Duration MIN_USAGE = Duration.ofMinutes(75);
    private static final LocalDate DATE = LocalDate.of(2026, 9, 14);
    private static final LocalDateTime BLOCK_START = LocalDateTime.of(DATE, LocalTime.of(14, 0));
    private static final LocalDateTime BLOCK_END = LocalDateTime.of(DATE, LocalTime.of(16, 0));

    @Test
    void lastBookableInstantForTheTwoToFourBlockIsFourteenThirty() {
        LocalTime cutoff = ReservationTimingCalculator.lastBookableInstant(LocalTime.of(16, 0), MIN_USAGE, TOLERANCE);

        assertThat(cutoff).isEqualTo(LocalTime.of(14, 30));
    }

    @Test
    void blockStopsBeingOfferedAfterFourteenThirty() {
        LocalDateTime justBefore = LocalDateTime.of(DATE, LocalTime.of(14, 30));
        LocalDateTime justAfter = LocalDateTime.of(DATE, LocalTime.of(14, 31));

        assertThat(ReservationTimingCalculator.isBlockStillBookable(DATE, LocalTime.of(16, 0), justBefore, MIN_USAGE, TOLERANCE))
                .isTrue();
        assertThat(ReservationTimingCalculator.isBlockStillBookable(DATE, LocalTime.of(16, 0), justAfter, MIN_USAGE, TOLERANCE))
                .isFalse();
    }

    @Test
    void earlyReservationOpensFifteenMinutesBeforeAndClosesFifteenAfter() {
        LocalDateTime createdYesterday = LocalDateTime.of(DATE.minusDays(1), LocalTime.of(9, 0));

        assertThat(ReservationTimingCalculator.checkInOpensAt(BLOCK_START, createdYesterday, TOLERANCE))
                .isEqualTo(LocalDateTime.of(DATE, LocalTime.of(13, 45)));
        assertThat(ReservationTimingCalculator.checkInClosesAt(BLOCK_START, BLOCK_END, createdYesterday, TOLERANCE, MIN_USAGE))
                .isEqualTo(LocalDateTime.of(DATE, LocalTime.of(14, 15)));
    }

    @Test
    void lastMinuteReservationCreatedAtFourteenTwentyTwoOpensImmediatelyAndClosesAtFourteenThirtySeven() {
        LocalDateTime createdAt = LocalDateTime.of(DATE, LocalTime.of(14, 22));

        assertThat(ReservationTimingCalculator.checkInOpensAt(BLOCK_START, createdAt, TOLERANCE)).isEqualTo(createdAt);
        assertThat(ReservationTimingCalculator.checkInClosesAt(BLOCK_START, BLOCK_END, createdAt, TOLERANCE, MIN_USAGE))
                .isEqualTo(LocalDateTime.of(DATE, LocalTime.of(14, 37)));
    }

    @Test
    void lastPossibleReservationCreatedAtFourteenThirtyClosesAtFourteenFortyFive() {
        LocalDateTime createdAt = LocalDateTime.of(DATE, LocalTime.of(14, 30));

        assertThat(ReservationTimingCalculator.checkInOpensAt(BLOCK_START, createdAt, TOLERANCE)).isEqualTo(createdAt);
        assertThat(ReservationTimingCalculator.checkInClosesAt(BLOCK_START, BLOCK_END, createdAt, TOLERANCE, MIN_USAGE))
                .isEqualTo(LocalDateTime.of(DATE, LocalTime.of(14, 45)));
    }

    @Test
    void earlyReservationTimelineMatchesSpecMilestones() {
        LocalDateTime createdYesterday = LocalDateTime.of(DATE.minusDays(1), LocalTime.of(9, 0));

        // Still reserved right up to the 14:15 check-in close.
        assertThat(stateAt(LocalTime.of(14, 0), createdYesterday, null, null)).isEqualTo(ReservationState.RESERVED);
        assertThat(stateAt(LocalTime.of(14, 14), createdYesterday, null, null)).isEqualTo(ReservationState.RESERVED);
        // No-show past the close: expired, not cancelled.
        assertThat(stateAt(LocalTime.of(14, 16), createdYesterday, null, null)).isEqualTo(ReservationState.EXPIRED);
    }

    @Test
    void fullWalkthroughAnaExpiresLuisBooksLastMinuteAndChecksIn() {
        // Ana: reserved yesterday, never shows up -> expires once 14:15 passes.
        LocalDateTime anaCreatedAt = LocalDateTime.of(DATE.minusDays(1), LocalTime.of(9, 0));
        assertThat(stateAt(LocalTime.of(14, 10), anaCreatedAt, null, null)).isEqualTo(ReservationState.RESERVED);
        assertThat(stateAt(LocalTime.of(14, 16), anaCreatedAt, null, null)).isEqualTo(ReservationState.EXPIRED);

        // Luis: books at 14:22 (last-minute), must check in by 14:37.
        LocalDateTime luisCreatedAt = LocalDateTime.of(DATE, LocalTime.of(14, 22));
        assertThat(stateAt(LocalTime.of(14, 22), luisCreatedAt, null, null)).isEqualTo(ReservationState.RESERVED);

        // Luis checks in at 14:24 -> in progress until the block ends at 16:00.
        LocalDateTime luisCheckedInAt = LocalDateTime.of(DATE, LocalTime.of(14, 24));
        assertThat(stateAt(LocalTime.of(14, 24), luisCreatedAt, luisCheckedInAt, null)).isEqualTo(ReservationState.IN_PROGRESS);
        assertThat(stateAt(LocalTime.of(15, 59), luisCreatedAt, luisCheckedInAt, null)).isEqualTo(ReservationState.IN_PROGRESS);

        // Block ends at 16:00 -> finished, not in progress anymore.
        assertThat(stateAt(LocalTime.of(16, 0), luisCreatedAt, luisCheckedInAt, null)).isEqualTo(ReservationState.FINISHED);
    }

    @Test
    void cancelledAlwaysWinsRegardlessOfTiming() {
        LocalDateTime createdAt = LocalDateTime.of(DATE, LocalTime.of(9, 0));
        LocalDateTime cancelledAt = LocalDateTime.of(DATE, LocalTime.of(10, 0));

        assertThat(stateAt(LocalTime.of(14, 5), createdAt, null, cancelledAt)).isEqualTo(ReservationState.CANCELLED);
    }

    private static ReservationState stateAt(
            LocalTime now, LocalDateTime createdAt, LocalDateTime checkedInAt, LocalDateTime cancelledAt) {
        return ReservationTimingCalculator.stateAt(
                LocalDateTime.of(DATE, now), BLOCK_START, BLOCK_END, createdAt, checkedInAt, cancelledAt, TOLERANCE, MIN_USAGE);
    }
}
