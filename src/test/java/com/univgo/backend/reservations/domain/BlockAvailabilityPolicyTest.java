package com.univgo.backend.reservations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.univgo.backend.spaces.domain.ClosureCause;
import com.univgo.backend.spaces.domain.ClosurePeriod;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.ClosureReversion;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.SpaceDetails;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Plain-Java coverage for {@link BlockAvailabilityPolicy} — no Spring context, no mocks. */
class BlockAvailabilityPolicyTest {

    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    private static final LocalDate DATE = LocalDate.of(2026, 9, 14);
    private static final TimeBlock BLOCK = new TimeBlock(LocalTime.of(14, 0), LocalTime.of(16, 0));
    private static final UUID SPACE_ID = UUID.randomUUID();

    private final BlockAvailabilityPolicy policy = new BlockAvailabilityPolicy();

    @Test
    void happyPathIsOffered() {
        LocalDateTime now = LocalDateTime.of(DATE, LocalTime.of(9, 0));
        AvailabilityContext context = contextAt(now, List.of(), StudentDay.of(List.of()), SpaceClosures.none());

        BlockAvailability result = policy.evaluate(space(2), BLOCK, context);

        assertThat(result.occupied()).isZero();
        assertThat(result.free()).isEqualTo(2);
        assertThat(result.offered()).isTrue();
        assertThat(result.closed()).isFalse();
        assertThat(result.alreadyReservedByUserToday()).isFalse();
        assertThat(result.overlapsUserReservation()).isFalse();
    }

    @Test
    void blockAlreadyStartedIsNotOffered() {
        // Bookable cutoff is blockEnd(16:00) - minUsage(75min) - tolerance(15min) = 14:30.
        LocalDateTime now = LocalDateTime.of(DATE, LocalTime.of(15, 0));
        AvailabilityContext context = contextAt(now, List.of(), StudentDay.of(List.of()), SpaceClosures.none());

        BlockAvailability result = policy.evaluate(space(2), BLOCK, context);

        assertThat(result.offered()).isFalse();
    }

    @Test
    void fullCapacityIsNotOffered() {
        LocalDateTime now = LocalDateTime.of(DATE, LocalTime.of(9, 0));
        Reservation occupying = reservation(SPACE_ID, UUID.randomUUID(), BLOCK, now.minusMinutes(5));
        AvailabilityContext context = contextAt(now, List.of(occupying), StudentDay.of(List.of()), SpaceClosures.none());

        BlockAvailability result = policy.evaluate(space(1), BLOCK, context);

        assertThat(result.occupied()).isEqualTo(1);
        assertThat(result.free()).isZero();
        assertThat(result.offered()).isFalse();
    }

    @Test
    void studentAtDailyLimitIsNotOffered() {
        LocalDateTime now = LocalDateTime.of(DATE, LocalTime.of(9, 0));
        // A reservation at a different block of the same space is enough to hit the daily limit,
        // without also tripping the overlap check.
        TimeBlock otherBlock = new TimeBlock(LocalTime.of(10, 0), LocalTime.of(11, 0));
        Reservation earlierToday = reservation(SPACE_ID, UUID.randomUUID(), otherBlock, now);
        AvailabilityContext context =
                contextAt(now, List.of(), StudentDay.of(List.of(earlierToday)), SpaceClosures.none());

        BlockAvailability result = policy.evaluate(space(2), BLOCK, context);

        assertThat(result.alreadyReservedByUserToday()).isTrue();
        assertThat(result.overlapsUserReservation()).isFalse();
        assertThat(result.offered()).isFalse();
    }

    @Test
    void overlappingReservationIsNotOffered() {
        LocalDateTime now = LocalDateTime.of(DATE, LocalTime.of(9, 0));
        // A different space, so only the overlap rule fires, not the daily limit.
        TimeBlock overlappingBlock = new TimeBlock(LocalTime.of(13, 0), LocalTime.of(15, 0));
        Reservation elsewhere = reservation(UUID.randomUUID(), UUID.randomUUID(), overlappingBlock, now);
        AvailabilityContext context =
                contextAt(now, List.of(), StudentDay.of(List.of(elsewhere)), SpaceClosures.none());

        BlockAvailability result = policy.evaluate(space(2), BLOCK, context);

        assertThat(result.overlapsUserReservation()).isTrue();
        assertThat(result.alreadyReservedByUserToday()).isFalse();
        assertThat(result.offered()).isFalse();
    }

    @Test
    void closedBlockIsShownWithItsReasonAndNotOffered() {
        LocalDateTime now = LocalDateTime.of(DATE, LocalTime.of(9, 0));
        SpaceClosure closure = new SpaceClosure(
                UUID.randomUUID(),
                SPACE_ID,
                new ClosurePeriod(LocalDateTime.of(DATE, LocalTime.MIDNIGHT), null),
                new ClosureCause(ClosureReason.MAINTENANCE, null),
                UUID.randomUUID(),
                now,
                ClosureReversion.none());
        AvailabilityContext context =
                contextAt(now, List.of(), StudentDay.of(List.of()), SpaceClosures.of(List.of(closure)));

        BlockAvailability result = policy.evaluate(space(2), BLOCK, context);

        assertThat(result.closed()).isTrue();
        assertThat(result.closureReason()).isEqualTo(ClosureReason.MAINTENANCE);
        assertThat(result.offered()).isFalse();
        // A block that is merely absent reads as "the space closes at ten"; this one says why.
        assertThat(result.free()).isEqualTo(2);
    }

    private static AvailabilityContext contextAt(
            LocalDateTime now, List<Reservation> activeInSpace, StudentDay studentDay, SpaceClosures closures) {
        return new AvailabilityContext(DATE, now, CONFIG, studentDay, BlockReservations.of(activeInSpace), closures);
    }

    private static Space space(int capacity) {
        return new Space(
                SPACE_ID, "Gimnasio", "Bloque A", capacity, UUID.randomUUID(), SpaceCategory.SPORTS,
                new SpaceDetails("desc", List.of()));
    }

    private static Reservation reservation(UUID spaceId, UUID userId, TimeBlock block, LocalDateTime createdAt) {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                userId,
                spaceId,
                new ReservationSchedule(DATE, block),
                createdAt,
                ReservationCheckpoint.initial());
    }
}
