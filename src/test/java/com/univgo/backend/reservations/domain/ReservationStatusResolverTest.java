package com.univgo.backend.reservations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceClosures;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReservationStatusResolverTest {

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ADMIN_ID = UUID.randomUUID();

    private static final LocalDate DAY = LocalDate.of(2026, 9, 17);
    private static final LocalTime BLOCK_START = LocalTime.of(14, 0);
    private static final LocalTime BLOCK_END = LocalTime.of(16, 0);

    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);

    private static final LocalDateTime BEFORE_THE_BLOCK = LocalDateTime.of(DAY, LocalTime.of(10, 0));
    private static final LocalDateTime AFTER_THE_BLOCK = LocalDateTime.of(DAY, LocalTime.of(18, 0));

    @Test
    void withoutClosuresItIsTheReservationsOwnState() {
        ReservationStatus status =
                ReservationStatusResolver.resolve(reserved(), SpaceClosures.none(), BEFORE_THE_BLOCK, CONFIG);

        assertThat(status.state()).isEqualTo(ReservationState.RESERVED);
        assertThat(status.closureReason()).isNull();
    }

    @Test
    void aClosureOverTheBlockSuspendsItWhileTheBlockIsStillToCome() {
        ReservationStatus status =
                ReservationStatusResolver.resolve(reserved(), closures(), BEFORE_THE_BLOCK, CONFIG);

        assertThat(status.state()).isEqualTo(ReservationState.SUSPENDED);
        assertThat(status.closureReason()).isEqualTo(ClosureReason.TECHNICAL_INCIDENT);
    }

    @Test
    void aBlockThatEndedUnderAClosureIsCancelledByTheSpace_notExpired() {
        // Expiring is not having turned up, and the door was shut: the student did nothing wrong,
        // so it reads as cancelled by the university, with its reason (spec §12).
        ReservationStatus status = ReservationStatusResolver.resolve(reserved(), closures(), AFTER_THE_BLOCK, CONFIG);

        assertThat(status.state()).isEqualTo(ReservationState.CANCELLED);
        assertThat(status.cancelledBy()).isEqualTo(CancelledBy.ADMIN);
        assertThat(status.closureReason()).isEqualTo(ClosureReason.TECHNICAL_INCIDENT);
    }

    @Test
    void withoutTheClosureThatSameBlockWouldHaveExpired() {
        ReservationStatus status =
                ReservationStatusResolver.resolve(reserved(), SpaceClosures.none(), AFTER_THE_BLOCK, CONFIG);

        assertThat(status.state()).isEqualTo(ReservationState.EXPIRED);
    }

    @Test
    void somebodyAlreadyInsideKeepsTheirBlockWhateverTheClosureSays() {
        Reservation checkedIn = reservation(LocalDateTime.of(DAY, LocalTime.of(14, 5)), null, null);

        ReservationStatus during = ReservationStatusResolver.resolve(
                checkedIn, closures(), LocalDateTime.of(DAY, LocalTime.of(15, 0)), CONFIG);

        assertThat(during.state()).isEqualTo(ReservationState.IN_PROGRESS);
        assertThat(during.closureReason()).isNull();
    }

    @Test
    void aCancellationIsAFactAClosureCannotUndo() {
        Reservation cancelled =
                reservation(null, LocalDateTime.of(DAY, LocalTime.of(9, 0)), CancelledBy.STUDENT);

        ReservationStatus status = ReservationStatusResolver.resolve(cancelled, closures(), BEFORE_THE_BLOCK, CONFIG);

        assertThat(status.state()).isEqualTo(ReservationState.CANCELLED);
        assertThat(status.cancelledBy()).isEqualTo(CancelledBy.STUDENT);
    }

    @Test
    void aRevertedClosureHandsTheReservationBackUntouched() {
        SpaceClosure closure = closure();
        closure.revert(ADMIN_ID, LocalDateTime.of(DAY, LocalTime.of(11, 0)));

        ReservationStatus status = ReservationStatusResolver.resolve(
                reserved(), SpaceClosures.of(List.of(closure)), BEFORE_THE_BLOCK, CONFIG);

        assertThat(status.state()).isEqualTo(ReservationState.RESERVED);
    }

    private static SpaceClosures closures() {
        return SpaceClosures.of(List.of(closure()));
    }

    private static SpaceClosure closure() {
        return new SpaceClosure(
                UUID.randomUUID(),
                SPACE_ID,
                LocalDateTime.of(DAY, LocalTime.of(8, 0)),
                LocalDateTime.of(DAY, LocalTime.of(20, 0)),
                ClosureReason.TECHNICAL_INCIDENT,
                "Gotera en la cancha",
                ADMIN_ID,
                LocalDateTime.of(DAY, LocalTime.of(7, 0)),
                null,
                null);
    }

    private static Reservation reserved() {
        return reservation(null, null, null);
    }

    private static Reservation reservation(LocalDateTime checkedInAt, LocalDateTime cancelledAt, CancelledBy actor) {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                USER_ID,
                SPACE_ID,
                DAY,
                BLOCK_START,
                BLOCK_END,
                LocalDateTime.of(DAY.minusDays(1), LocalTime.of(10, 0)),
                checkedInAt,
                cancelledAt,
                actor);
    }
}
