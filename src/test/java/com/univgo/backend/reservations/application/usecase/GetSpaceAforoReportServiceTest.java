package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.in.GetSpaceDayBlocksUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockAforoRow;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.reservations.domain.CancelledBy;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.SpaceBlockSummary;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.TimeBlock;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSpaceAforoReportServiceTest {

    @Mock
    private GetSpaceDayBlocksUseCase getSpaceDayBlocksUseCase;

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    @Mock
    private SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private GetSpaceAforoReportService service;

    @BeforeEach
    void setUp() {
        service = new GetSpaceAforoReportService(
                getSpaceDayBlocksUseCase,
                spaceRepositoryPort,
                reservationRepositoryPort,
                institutionConfigRepositoryPort,
                spaceClosureRepositoryPort,
                userRepositoryPort,
                Clock.systemDefaultZone());
    }

    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    // In the past, so a reservation nobody checked in to has expired by now.
    private static final LocalDate PAST_DATE = LocalDate.now().minusDays(7);
    private static final UUID STUDENT_ID = UUID.randomUUID();
    private static final TimeBlock BLOCK = new TimeBlock(LocalTime.of(14, 0), LocalTime.of(16, 0));

    @Test
    void countsHowEachBlocksReservationsEndedAndListsWhoMadeThem() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(new Space(
                SPACE_ID, "Gimnasio", "Bloque A", 30, UUID.randomUUID(), SpaceCategory.SPORTS, "", List.of())));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of());
        LocalDateTime start = LocalDateTime.of(PAST_DATE, BLOCK.start());
        when(userRepositoryPort.findById(STUDENT_ID)).thenReturn(Optional.of(new User(
                STUDENT_ID, "1001", "ana@univ.edu", "Ana", "Pérez", "x", Set.of("STUDENT"), "Ingeniería")));
        when(reservationRepositoryPort.findBySpaceAndDate(SPACE_ID, PAST_DATE))
                .thenReturn(List.of(
                        reservation(start.plusMinutes(5), null, null),
                        reservation(null, null, null),
                        reservation(null, start.minusHours(3), CancelledBy.STUDENT)));
        when(getSpaceDayBlocksUseCase.execute(SPACE_ID, PAST_DATE))
                .thenReturn(List.of(new SpaceBlockSummary(BLOCK, 30, 0, 30, false, null)));

        var report = service.execute(SPACE_ID, PAST_DATE);

        assertThat(report.spaceName()).isEqualTo("Gimnasio");
        BlockAforoRow row = report.rows().getFirst();
        assertThat(List.of(row.capacity(), row.created(), row.checkedIn(), row.expired(), row.cancelled(), row.free()))
                .containsExactly(30, 3, 1, 1, 1, 30);
        assertThat(row.roster()).extracting(o -> o.state()).containsExactlyInAnyOrder(
                ReservationState.FINISHED, ReservationState.EXPIRED, ReservationState.CANCELLED);
        assertThat(row.roster().getFirst().studentName()).isEqualTo("Ana Pérez");
        assertThat(row.roster().getFirst().document()).isEqualTo("1001");
        assertThat(row.roster().getFirst().school()).isEqualTo("Ingeniería");
    }


    private static Reservation reservation(
            LocalDateTime checkedInAt, LocalDateTime cancelledAt, CancelledBy cancelledBy) {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "123456",
                STUDENT_ID,
                SPACE_ID,
                PAST_DATE,
                BLOCK.start(),
                BLOCK.end(),
                LocalDateTime.of(PAST_DATE.minusDays(1), LocalTime.NOON),
                checkedInAt,
                cancelledAt,
                cancelledBy);
    }
}
