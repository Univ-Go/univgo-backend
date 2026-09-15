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
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
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

    @InjectMocks
    private GetSpaceAvailabilityService service;

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    // Far enough ahead that every block is still bookable no matter when this test runs.
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    void reportsFreePlazasBasedOnCapacityMinusActiveReservations() {
        Space space = new Space(SPACE_ID, "Gimnasio", 30, UUID.randomUUID(), false);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(new SpaceSchedule(UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(14, 0), LocalTime.of(16, 0))));
        when(reservationRepositoryPort.countActiveByUserSpaceAndDate(USER_ID, SPACE_ID, FUTURE_DATE)).thenReturn(0L);
        when(reservationRepositoryPort.existsOverlappingForUser(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepositoryPort.findActiveByBlock(SPACE_ID, FUTURE_DATE, LocalTime.of(14, 0), LocalTime.of(16, 0)))
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
        Space space = new Space(SPACE_ID, "Gimnasio", 1, UUID.randomUUID(), false);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(new SpaceSchedule(UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(14, 0), LocalTime.of(16, 0))));
        when(reservationRepositoryPort.countActiveByUserSpaceAndDate(USER_ID, SPACE_ID, FUTURE_DATE)).thenReturn(0L);
        when(reservationRepositoryPort.existsOverlappingForUser(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepositoryPort.findActiveByBlock(SPACE_ID, FUTURE_DATE, LocalTime.of(14, 0), LocalTime.of(16, 0)))
                .thenReturn(List.of(activeReservation()));

        BlockAvailability availability = service.execute(SPACE_ID, FUTURE_DATE, USER_ID).getFirst();

        assertThat(availability.free()).isZero();
        assertThat(availability.offered()).isFalse();
    }

    @Test
    void blockIsNotOfferedWhenAlreadyReservedTodayOrOverlapping() {
        Space space = new Space(SPACE_ID, "Gimnasio", 30, UUID.randomUUID(), false);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(new SpaceSchedule(UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(14, 0), LocalTime.of(16, 0))));
        when(reservationRepositoryPort.countActiveByUserSpaceAndDate(USER_ID, SPACE_ID, FUTURE_DATE)).thenReturn(1L);
        when(reservationRepositoryPort.existsOverlappingForUser(any(), any(), any(), any())).thenReturn(true);
        when(reservationRepositoryPort.findActiveByBlock(any(), any(), any(), any())).thenReturn(List.of());

        BlockAvailability availability = service.execute(SPACE_ID, FUTURE_DATE, USER_ID).getFirst();

        assertThat(availability.alreadyReservedByUserToday()).isTrue();
        assertThat(availability.overlapsUserReservation()).isTrue();
        assertThat(availability.offered()).isFalse();
    }

    @Test
    void spaceUnderMaintenanceOffersNoBlocks() {
        Space space = new Space(SPACE_ID, "Gimnasio", 30, UUID.randomUUID(), true);
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));

        List<BlockAvailability> result = service.execute(SPACE_ID, FUTURE_DATE, USER_ID);

        assertThat(result).isEmpty();
    }

    private static Reservation activeReservation() {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
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
