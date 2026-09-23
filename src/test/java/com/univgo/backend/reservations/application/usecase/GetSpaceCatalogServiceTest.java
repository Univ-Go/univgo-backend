package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationCheckpoint;
import com.univgo.backend.reservations.domain.ReservationSchedule;
import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceImageRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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

    @Mock
    private SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    @Mock
    private SpaceImageRepositoryPort spaceImageRepositoryPort;

    @Spy
    private Clock clock = Clock.systemDefaultZone();

    @InjectMocks
    private GetSpaceCatalogService service;

    @BeforeEach
    void noImagesUnlessATestSaysOtherwise() {
        lenient().when(spaceImageRepositoryPort.findAllImageUrls()).thenReturn(Map.of());
    }

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    // Far enough ahead that every block is still bookable no matter when this test runs.
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);
    private static final LocalDate PAST_DATE = LocalDate.now().minusDays(1);

    @Test
    void describesTheSpaceAndListsEveryBlockThatStillHasRoom() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30)));
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
        assertThat(item.opensOnDate()).isTrue();
        assertThat(item.closedOnDate()).isFalse();
        assertThat(item.freeBlockStarts()).containsExactly(LocalTime.of(14, 0), LocalTime.of(16, 0));
        assertThat(item.description()).isEqualTo("Sala de musculación y cardio");
        assertThat(item.rules()).containsExactly("Usa toalla sobre las máquinas");
    }

    @Test
    void readsOneSpaceTheSameWayItReadsTheCatalog() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of());

        SpaceCatalogItem item = service.execute(SPACE_ID, FUTURE_DATE);

        assertThat(item.spaceId()).isEqualTo(SPACE_ID);
        assertThat(item.description()).isEqualTo("Sala de musculación y cardio");
        assertThat(item.rules()).containsExactly("Usa toalla sobre las máquinas");
        assertThat(item.freeBlockStarts()).containsExactly(LocalTime.of(14, 0), LocalTime.of(16, 0));
    }

    @Test
    void refusesToDescribeASpaceThatIsNotThere() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(SPACE_ID, FUTURE_DATE))
                .isInstanceOf(SpaceNotFoundException.class);
    }

    @Test
    void leavesOutBlocksWhoseCapacityIsFull() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(1)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of(activeReservation()));

        List<SpaceCatalogItem> result = service.execute(FUTURE_DATE);

        assertThat(result.getFirst().freeBlockStarts()).containsExactly(LocalTime.of(16, 0));
    }

    @Test
    void offersNothingOnADayThatAlreadyPassed() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(PAST_DATE)).thenReturn(List.of());

        List<SpaceCatalogItem> result = service.execute(PAST_DATE);

        // It opened and it is not shut: what is gone is the day, and that is a third thing.
        assertThat(result.getFirst().opensOnDate()).isTrue();
        assertThat(result.getFirst().closedOnDate()).isFalse();
        assertThat(result.getFirst().freeBlockStarts()).isEmpty();
    }

    @Test
    void aSpaceClosedWithNoEndDateOffersNoBlocksAndSaysSo() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of());
        when(spaceClosureRepositoryPort.findAllInForce()).thenReturn(List.of(indefiniteClosure()));

        List<SpaceCatalogItem> result = service.execute(FUTURE_DATE);

        assertThat(result.getFirst().underMaintenance()).isTrue();
        assertThat(result.getFirst().closedOnDate()).isTrue();
        assertThat(result.getFirst().freeBlockStarts()).isEmpty();
    }

    @Test
    void aClosureOfOneAfternoonOnlyTakesOutTheBlocksItCovers() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of());
        when(spaceClosureRepositoryPort.findAllInForce())
                .thenReturn(List.of(closure(
                        LocalDateTime.of(FUTURE_DATE, LocalTime.of(14, 0)),
                        LocalDateTime.of(FUTURE_DATE, LocalTime.of(16, 0)))));

        List<SpaceCatalogItem> result = service.execute(FUTURE_DATE);

        // The space is open right now — the closure is a week away — so only its blocks go.
        assertThat(result.getFirst().underMaintenance()).isFalse();
        assertThat(result.getFirst().freeBlockStarts()).containsExactly(LocalTime.of(16, 0));
    }

    @Test
    void aSpaceWithNoPhotographsUploadedGetsAnEmptyList() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of());

        List<SpaceCatalogItem> result = service.execute(FUTURE_DATE);

        assertThat(result.getFirst().images()).isEmpty();
    }

    @Test
    void carriesTheSpacesPhotographsFromTheImagePort() {
        when(spaceRepositoryPort.findAll()).thenReturn(List.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findByDayOfWeek(anyInt())).thenReturn(openFrom(14, 18));
        when(reservationRepositoryPort.findActiveByDate(FUTURE_DATE)).thenReturn(List.of());
        when(spaceImageRepositoryPort.findAllImageUrls())
                .thenReturn(Map.of(SPACE_ID, List.of("https://example.com/cover.webp")));

        List<SpaceCatalogItem> result = service.execute(FUTURE_DATE);

        assertThat(result.getFirst().images()).containsExactly("https://example.com/cover.webp");
    }

    private static SpaceClosure indefiniteClosure() {
        return closure(LocalDateTime.now().minusHours(1), null);
    }

    private static SpaceClosure closure(LocalDateTime startsAt, LocalDateTime endsAt) {
        return new SpaceClosure(
                UUID.randomUUID(),
                SPACE_ID,
                startsAt,
                endsAt,
                ClosureReason.MAINTENANCE,
                null,
                UUID.randomUUID(),
                LocalDateTime.now(),
                null,
                null);
    }

    private static Space space(int capacity) {
        return new Space(
                SPACE_ID,
                "Gimnasio",
                "Complejo Deportivo Central",
                capacity,
                UUID.randomUUID(),
                SpaceCategory.SPORTS,
                "Sala de musculación y cardio",
                List.of("Usa toalla sobre las máquinas"));
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
                new ReservationSchedule(FUTURE_DATE, new TimeBlock(LocalTime.of(14, 0), LocalTime.of(16, 0))),
                LocalDateTime.now(),
                ReservationCheckpoint.initial());
    }
}
