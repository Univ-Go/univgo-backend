package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSpaceCatalogServiceTest {

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    @InjectMocks
    private GetSpaceCatalogService service;

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    // Far enough ahead that every block is still bookable no matter when this test runs.
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);
    private static final LocalDate PAST_DATE = LocalDate.now().minusDays(1);

    @Test
    void describesTheSpaceAndListsEveryBlockThatStillHasRoom() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30, false)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of());

        List<SpaceCatalogItem> result = service.execute(FUTURE_DATE);

        assertThat(result).hasSize(1);
        SpaceCatalogItem item = result.getFirst();
        assertThat(item.spaceId()).isEqualTo(SPACE_ID);
        assertThat(item.name()).isEqualTo("Gimnasio");
        assertThat(item.location()).isEqualTo("Complejo Deportivo Central");
        assertThat(item.category()).isEqualTo(SpaceCategory.SPORTS);
        assertThat(item.capacity()).isEqualTo(30);
        assertThat(item.underMaintenance()).isFalse();
        assertThat(item.freeBlockStarts()).containsExactly(LocalTime.of(14, 0), LocalTime.of(16, 0));
    }

    @Test
    void leavesOutBlocksWhoseCapacityIsFull() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(1, false)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of(activeReservation()));

        List<SpaceCatalogItem> result = service.execute(FUTURE_DATE);

        assertThat(result.getFirst().freeBlockStarts()).containsExactly(LocalTime.of(16, 0));
    }

    @Test
    void offersNothingOnADayThatAlreadyPassed() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30, false)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(PAST_DATE)).thenReturn(List.of());

        List<SpaceCatalogItem> result = service.execute(PAST_DATE);

        assertThat(result.getFirst().freeBlockStarts()).isEmpty();
    }

    @Test
    void aSpaceUnderMaintenanceOffersNoBlocks() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30, true)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of());

        List<SpaceCatalogItem> result = service.execute(FUTURE_DATE);

        assertThat(result.getFirst().underMaintenance()).isTrue();
        assertThat(result.getFirst().freeBlockStarts()).isEmpty();
    }

    private static Space space(int capacity, boolean underMaintenance) {
        return new Space(
                SPACE_ID,
                "Gimnasio",
                "Complejo Deportivo Central",
                capacity,
                UUID.randomUUID(),
                SpaceCategory.SPORTS,
                underMaintenance);
    }

    private static List<SpaceSchedule> openFrom(int startHour, int endHour) {
        return List.of(new SpaceSchedule(
                UUID.randomUUID(), SPACE_ID, 1, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0)));
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
