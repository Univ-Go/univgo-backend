package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.SpaceDaySummary;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSpaceBlocksRangeSummaryServiceTest {

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

    private GetSpaceBlocksRangeSummaryService service;

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    private static final LocalDate DAY_1 = LocalDate.now().plusDays(7);
    private static final LocalDate DAY_2 = DAY_1.plusDays(1);
    private static final LocalDate DAY_3 = DAY_1.plusDays(2);

    @BeforeEach
    void setUp() {
        service = new GetSpaceBlocksRangeSummaryService(
                spaceRepositoryPort,
                spaceScheduleRepositoryPort,
                reservationRepositoryPort,
                institutionConfigRepositoryPort,
                spaceClosureRepositoryPort,
                Clock.systemDefaultZone());
    }

    @Test
    void sumsCapacityAndFreeSeatsPerDayAcrossTheRange_zeroOnDaysWithNoBlocks() {
        Space space = Space.builder()
                .id(SPACE_ID)
                .name("Gimnasio")
                .location("Bloque A")
                .capacity(30)
                .spaceTypeId(UUID.randomUUID())
                .category(SpaceCategory.SPORTS)
                .description("desc")
                .rules(List.of())
                .build();
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of());

        // DAY_2's day of week has no window, so it reads as closed; DAY_1 and DAY_3 both do.
        when(spaceScheduleRepositoryPort.findBySpaceId(SPACE_ID))
                .thenReturn(List.of(
                        new SpaceSchedule(
                                UUID.randomUUID(), SPACE_ID, DAY_1.getDayOfWeek().getValue(),
                                LocalTime.of(14, 0), LocalTime.of(16, 0)),
                        new SpaceSchedule(
                                UUID.randomUUID(), SPACE_ID, DAY_3.getDayOfWeek().getValue(),
                                LocalTime.of(14, 0), LocalTime.of(16, 0))));
        when(reservationRepositoryPort.findActiveBySpaceAndDateRange(SPACE_ID, DAY_1, DAY_3))
                .thenReturn(List.of(activeReservation(DAY_1), activeReservation(DAY_1), activeReservation(DAY_1)));

        List<SpaceDaySummary> result = service.execute(SPACE_ID, DAY_1, DAY_3);

        assertThat(result).containsExactly(
                new SpaceDaySummary(DAY_1, 30, 27),
                new SpaceDaySummary(DAY_2, 0, 0),
                new SpaceDaySummary(DAY_3, 30, 30));
    }

    @Test
    void rejectsAToBeforeFrom() {
        assertThatThrownBy(() -> service.execute(SPACE_ID, DAY_2, DAY_1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsRangesLongerThan14Days() {
        LocalDate fifteenthDay = DAY_1.plusDays(14);

        assertThatThrownBy(() -> service.execute(SPACE_ID, DAY_1, fifteenthDay))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Reservation activeReservation(LocalDate date) {
        return Reservation.builder()
                .id(UUID.randomUUID())
                .qrCodeData(UUID.randomUUID().toString())
                .userId(UUID.randomUUID())
                .spaceId(SPACE_ID)
                .reservationDate(date)
                .blockStart(LocalTime.of(14, 0))
                .blockEnd(LocalTime.of(16, 0))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
