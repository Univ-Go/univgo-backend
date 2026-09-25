package com.univgo.backend.reservations.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Exercises the "Casos límite" cancellation rows from the functional spec. */
class ReservationTest {

    private static final Duration TOLERANCE = Duration.ofMinutes(15);
    private static final Duration MIN_USAGE = Duration.ofMinutes(75);
    private static final LocalDate DATE = LocalDate.of(2026, 9, 14);

    @Test
    void cancellingBeforeTheBlockStartsSucceeds() {
        Reservation reservation = reservedYesterday();

        reservation.cancel(CancelledBy.STUDENT, LocalDateTime.of(DATE, LocalTime.of(13, 0)), TOLERANCE, MIN_USAGE);

        assertThat(reservation.getCancelledAt()).isNotNull();
        assertThat(reservation.getCancelledBy()).isEqualTo(CancelledBy.STUDENT);
    }

    @Test
    void cancellingWithinOneHourOfBlockStartIsRejectedForStudents() {
        Reservation reservation = reservedYesterday();

        // Block starts 14:00; cancellation window for a student closes at 13:00.
        assertThatThrownBy(() ->
                        reservation.cancel(CancelledBy.STUDENT, LocalDateTime.of(DATE, LocalTime.of(13, 30)), TOLERANCE, MIN_USAGE))
                .isInstanceOf(CancellationWindowClosedException.class);
    }

    @Test
    void cancellingExactlyOneHourBeforeBlockStartSucceeds() {
        Reservation reservation = reservedYesterday();

        reservation.cancel(CancelledBy.STUDENT, LocalDateTime.of(DATE, LocalTime.of(13, 0)), TOLERANCE, MIN_USAGE);

        assertThat(reservation.getCancelledAt()).isNotNull();
    }

    @Test
    void adminCanCancelWithinOneHourOfBlockStart() {
        Reservation reservation = reservedYesterday();

        // Block starts 14:00, check-in for an early reservation closes 14:15.
        reservation.cancel(CancelledBy.ADMIN, LocalDateTime.of(DATE, LocalTime.of(13, 30)), TOLERANCE, MIN_USAGE);

        assertThat(reservation.getCancelledAt()).isNotNull();
        assertThat(reservation.getCancelledBy()).isEqualTo(CancelledBy.ADMIN);
    }

    @Test
    void cancellingWhileInProgressIsRejectedRegardlessOfActor() {
        Reservation reservation = reservedYesterday();
        reservation.checkIn(LocalDateTime.of(DATE, LocalTime.of(14, 0)));

        assertThatThrownBy(() ->
                        reservation.cancel(CancelledBy.ADMIN, LocalDateTime.of(DATE, LocalTime.of(14, 30)), TOLERANCE, MIN_USAGE))
                .isInstanceOf(CannotCancelInProgressReservationException.class);
    }

    @Test
    void cancellingAnAlreadyExpiredReservationIsRejected() {
        Reservation reservation = reservedYesterday();

        assertThatThrownBy(() ->
                        reservation.cancel(CancelledBy.STUDENT, LocalDateTime.of(DATE, LocalTime.of(14, 20)), TOLERANCE, MIN_USAGE))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void cancellingAFinishedReservationIsRejected() {
        Reservation reservation = reservedYesterday();
        reservation.checkIn(LocalDateTime.of(DATE, LocalTime.of(14, 0)));

        assertThatThrownBy(() ->
                        reservation.cancel(CancelledBy.STUDENT, LocalDateTime.of(DATE, LocalTime.of(16, 0)), TOLERANCE, MIN_USAGE))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    @Test
    void cancellingAnAlreadyCancelledReservationIsRejected() {
        Reservation reservation = reservedYesterday();
        reservation.cancel(CancelledBy.STUDENT, LocalDateTime.of(DATE, LocalTime.of(13, 0)), TOLERANCE, MIN_USAGE);

        assertThatThrownBy(() ->
                        reservation.cancel(CancelledBy.STUDENT, LocalDateTime.of(DATE, LocalTime.of(13, 30)), TOLERANCE, MIN_USAGE))
                .isInstanceOf(InvalidReservationStateException.class);
    }

    private static Reservation reservedYesterday() {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new ReservationSchedule(DATE, new TimeBlock(LocalTime.of(14, 0), LocalTime.of(16, 0))),
                LocalDateTime.of(DATE.minusDays(1), LocalTime.of(9, 0)),
                ReservationCheckpoint.initial());
    }
}
