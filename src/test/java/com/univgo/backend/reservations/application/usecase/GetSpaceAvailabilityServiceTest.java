package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockAvailability;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSpaceAvailabilityServiceTest {

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    @Mock
    private SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    @InjectMocks
    private GetSpaceAvailabilityService service;

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    // Far enough ahead that every block is still bookable no matter when this test runs.
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    void reportsFreePlazasBasedOnCapacityMinusActiveReservations() {
        Space space = new Space(SPACE_ID, "Gimnasio", "Bloque A", 30, UUID.randomUUID(), SpaceCategory.SPORTS);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(new SpaceSchedule(UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(14, 0), LocalTime.of(16, 0))));
        when(reservationRepositoryPort.findActiveByUserAndDate(USER_ID, FUTURE_DATE)).thenReturn(List.of());
        when(reservationRepositoryPort.findActiveBySpaceAndDate(SPACE_ID, FUTURE_DATE))
                .thenReturn(List.of(activeReservation(), activeReservation(), activeReservation()));

        List<BlockAvailability> result = service.execute(SPACE_ID, FUTURE_DATE, USER_ID);

        assertThat(result).hasSize(1);
        BlockAvailability availability = result.getFirst();
        assertThat(availability.capacity()).isEqualTo(30);
        assertThat(availability.occupied()).isEqualTo(3);
        assertThat(availability.free()).isEqualTo(27);
        assertThat(availability.offered()).isTrue();
    }

    @Test
    void blockIsNotOfferedWhenFull() {
        Space space = new Space(SPACE_ID, "Gimnasio", "Bloque A", 1, UUID.randomUUID(), SpaceCategory.SPORTS);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(new SpaceSchedule(UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(14, 0), LocalTime.of(16, 0))));
        when(reservationRepositoryPort.findActiveByUserAndDate(USER_ID, FUTURE_DATE)).thenReturn(List.of());
        when(reservationRepositoryPort.findActiveBySpaceAndDate(SPACE_ID, FUTURE_DATE))
                .thenReturn(List.of(activeReservation()));

        BlockAvailability availability = service.execute(SPACE_ID, FUTURE_DATE, USER_ID).getFirst();

        assertThat(availability.free()).isZero();
        assertThat(availability.offered()).isFalse();
    }

    @Test
    void blockIsNotOfferedWhenAlreadyReservedTodayOrOverlapping() {
        Space space = new Space(SPACE_ID, "Gimnasio", "Bloque A", 30, UUID.randomUUID(), SpaceCategory.SPORTS);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(new SpaceSchedule(UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(14, 0), LocalTime.of(16, 0))));
        // The student's own reservation in this very block answers both questions at once, which is
        // what the data has always said: a clash with oneself is also the day's reservation.
        when(reservationRepositoryPort.findActiveByUserAndDate(USER_ID, FUTURE_DATE))
                .thenReturn(List.of(reservationOf(USER_ID)));
        when(reservationRepositoryPort.findActiveBySpaceAndDate(SPACE_ID, FUTURE_DATE)).thenReturn(List.of());

        BlockAvailability availability = service.execute(SPACE_ID, FUTURE_DATE, USER_ID).getFirst();

        assertThat(availability.alreadyReservedByUserToday()).isTrue();
        assertThat(availability.overlapsUserReservation()).isTrue();
        assertThat(availability.offered()).isFalse();
    }

    @Test
    void aClosedBlockIsShownAndRefused_ratherThanMissingFromTheGrid() {
        Space space = new Space(SPACE_ID, "Gimnasio", "Bloque A", 30, UUID.randomUUID(), SpaceCategory.SPORTS);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(new SpaceSchedule(UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(14, 0), LocalTime.of(16, 0))));
        when(reservationRepositoryPort.findActiveBySpaceAndDate(SPACE_ID, FUTURE_DATE)).thenReturn(List.of());
        when(reservationRepositoryPort.findActiveByUserAndDate(USER_ID, FUTURE_DATE)).thenReturn(List.of());
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of(indefiniteClosure()));

        BlockAvailability availability = service.execute(SPACE_ID, FUTURE_DATE, USER_ID).getFirst();

        assertThat(availability.closed()).isTrue();
        assertThat(availability.offered()).isFalse();
        // A block that is merely absent reads as "the space closes at ten"; this one says why.
        assertThat(availability.free()).isEqualTo(30);
    }

    private static SpaceClosure indefiniteClosure() {
        return new SpaceClosure(
                UUID.randomUUID(),
                SPACE_ID,
                LocalDateTime.now().minusHours(1),
                null,
                ClosureReason.TECHNICAL_INCIDENT,
                null,
                UUID.randomUUID(),
                LocalDateTime.now(),
                null,
                null);
    }

    private static Reservation activeReservation() {
        return reservationOf(UUID.randomUUID());
    }

    private static Reservation reservationOf(UUID userId) {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                userId,
                SPACE_ID,
                FUTURE_DATE,
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                LocalDateTime.now(),
                null,
                null,
                null);
    }
}
