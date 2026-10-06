package com.univgo.backend.reservations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SpacePenaltyTest {

    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    private static final LocalDate DATE = LocalDate.of(2026, 10, 1);
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SPACE_ID = UUID.randomUUID();

    @Test
    void unattendedBlockPenalizesUntilTwentyFourHoursAfterItsStart() {
        Reservation noShow = reservation(ReservationCheckpoint.initial());
        LocalDateTime now = LocalDateTime.of(DATE, LocalTime.of(10, 0));

        Optional<LocalDateTime> until = SpacePenalty.activeUntil(List.of(noShow), SpaceClosures.none(), now, CONFIG);

        assertThat(until).contains(LocalDateTime.of(DATE.plusDays(1), LocalTime.of(8, 0)));
    }

    @Test
    void penaltyIsGoneOnceItHasLifted() {
        Reservation noShow = reservation(ReservationCheckpoint.initial());
        LocalDateTime now = LocalDateTime.of(DATE.plusDays(1), LocalTime.of(8, 0));

        assertThat(SpacePenalty.activeUntil(List.of(noShow), SpaceClosures.none(), now, CONFIG)).isEmpty();
    }

    @Test
    void aStudentWhoCheckedInDoesNotPenalize() {
        ReservationCheckpoint checkedIn = ReservationCheckpoint.initial().withCheckIn(LocalDateTime.of(DATE, LocalTime.of(8, 5)));
        Reservation attended = reservation(checkedIn);
        LocalDateTime now = LocalDateTime.of(DATE, LocalTime.of(10, 0));

        assertThat(SpacePenalty.activeUntil(List.of(attended), SpaceClosures.none(), now, CONFIG)).isEmpty();
    }

    private static Reservation reservation(ReservationCheckpoint checkpoint) {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                USER_ID,
                SPACE_ID,
                new ReservationSchedule(DATE, new TimeBlock(LocalTime.of(8, 0), LocalTime.of(10, 0))),
                LocalDateTime.of(DATE, LocalTime.of(7, 0)),
                checkpoint);
    }
}
