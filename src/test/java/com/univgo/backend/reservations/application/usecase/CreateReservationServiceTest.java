package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase.CreateReservationCommand;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.BlockCapacityFullException;
import com.univgo.backend.reservations.domain.BlockNoLongerBookableException;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationOverlapException;
import com.univgo.backend.reservations.domain.SpaceAlreadyReservedTodayException;
import com.univgo.backend.reservations.domain.SpaceClosedException;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceScheduleRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceCategory;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateReservationServiceTest {

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;

    @Mock
    private SpaceScheduleRepositoryPort spaceScheduleRepositoryPort;

    @Mock
    private InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    @Mock
    private SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    @InjectMocks
    private CreateReservationService service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);

    // Far enough in the future that "isBlockStillBookable" is true no matter when this test runs.
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);
    private static final LocalTime BLOCK_START = LocalTime.of(14, 0);
    private static final LocalTime BLOCK_END = LocalTime.of(16, 0);

    private CreateReservationCommand futureCommand;

    @BeforeEach
    void setUp() {
        futureCommand = new CreateReservationCommand(USER_ID, SPACE_ID, FUTURE_DATE, BLOCK_START);
    }

    @Test
    void throwsWhenSpaceDoesNotExist() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(futureCommand)).isInstanceOf(SpaceNotFoundException.class);
    }

    @Test
    void throwsWhenTheBlockFallsInsideAClosure() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(schedule(BLOCK_START, BLOCK_END)));
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID)).thenReturn(List.of(closure(null)));

        assertThatThrownBy(() -> service.execute(futureCommand)).isInstanceOf(SpaceClosedException.class);
    }

    @Test
    void aClosureOfAnotherAfternoonDoesNotStopTheBooking() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(schedule(BLOCK_START, BLOCK_END)));
        when(spaceClosureRepositoryPort.findInForceBySpaceId(SPACE_ID))
                .thenReturn(List.of(closure(LocalDateTime.of(FUTURE_DATE.minusDays(1), BLOCK_START))));
        when(reservationRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.execute(futureCommand).getBlockStart()).isEqualTo(BLOCK_START);
    }

    /** The day's booking, held for a block of this same space that is not the one being asked for. */
    private static Reservation sameSpaceEarlierBlock() {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                USER_ID,
                SPACE_ID,
                FUTURE_DATE,
                LocalTime.of(8, 0),
                LocalTime.of(10, 0),
                LocalDateTime.now(),
                null,
                null,
                null);
    }

    /** {@code endsAt} null is the switch's own closure: shut until somebody reverts it. */
    private static SpaceClosure closure(LocalDateTime endsAt) {
        return new SpaceClosure(
                UUID.randomUUID(),
                SPACE_ID,
                LocalDateTime.now().minusHours(1),
                endsAt,
                ClosureReason.MAINTENANCE,
                null,
                UUID.randomUUID(),
                LocalDateTime.now(),
                null,
                null);
    }

    @Test
    void throwsWhenRequestedStartTimeIsNotAGeneratedBlock() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(schedule(LocalTime.of(6, 0), LocalTime.of(8, 0))));

        assertThatThrownBy(() -> service.execute(futureCommand)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwsWhenTheBlockIsPastTheLastBookableInstant() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        var command = new CreateReservationCommand(USER_ID, SPACE_ID, pastDate, BLOCK_START);

        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(schedule(BLOCK_START, BLOCK_END)));

        assertThatThrownBy(() -> service.execute(command)).isInstanceOf(BlockNoLongerBookableException.class);
    }

    @Test
    void throwsWhenStudentAlreadyReservedThatSpaceToday() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(schedule(BLOCK_START, BLOCK_END)));
        when(reservationRepositoryPort.findActiveByUserAndDate(USER_ID, FUTURE_DATE))
                .thenReturn(List.of(sameSpaceEarlierBlock()));

        assertThatThrownBy(() -> service.execute(futureCommand)).isInstanceOf(SpaceAlreadyReservedTodayException.class);
    }

    @Test
    void throwsWhenStudentHasAnOverlappingReservationInAnotherSpace() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(schedule(BLOCK_START, BLOCK_END)));
        when(reservationRepositoryPort.existsOverlappingForUser(USER_ID, FUTURE_DATE, BLOCK_START, BLOCK_END))
                .thenReturn(true);

        assertThatThrownBy(() -> service.execute(futureCommand)).isInstanceOf(ReservationOverlapException.class);
    }

    @Test
    void throwsWhenTheBlockHasNoFreePlazas() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(1)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(schedule(BLOCK_START, BLOCK_END)));
        when(reservationRepositoryPort.existsOverlappingForUser(USER_ID, FUTURE_DATE, BLOCK_START, BLOCK_END))
                .thenReturn(false);
        when(reservationRepositoryPort.findActiveByBlock(SPACE_ID, FUTURE_DATE, BLOCK_START, BLOCK_END))
                .thenReturn(List.of(activeReservation()));

        assertThatThrownBy(() -> service.execute(futureCommand)).isInstanceOf(BlockCapacityFullException.class);
    }

    @Test
    void happyPathSavesTheReservation() {
        when(spaceRepositoryPort.findById(SPACE_ID)).thenReturn(Optional.of(space(30)));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(spaceScheduleRepositoryPort.findBySpaceIdAndDayOfWeek(any(), anyInt()))
                .thenReturn(List.of(schedule(BLOCK_START, BLOCK_END)));
        when(reservationRepositoryPort.existsOverlappingForUser(USER_ID, FUTURE_DATE, BLOCK_START, BLOCK_END))
                .thenReturn(false);
        when(reservationRepositoryPort.findActiveByBlock(SPACE_ID, FUTURE_DATE, BLOCK_START, BLOCK_END))
                .thenReturn(List.of());
        when(reservationRepositoryPort.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = service.execute(futureCommand);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getSpaceId()).isEqualTo(SPACE_ID);
        assertThat(result.getBlockStart()).isEqualTo(BLOCK_START);
        assertThat(result.getBlockEnd()).isEqualTo(BLOCK_END);
        assertThat(result.getCheckedInAt()).isNull();
        assertThat(result.getCancelledAt()).isNull();
    }

    private static Space space(int capacity) {
        return new Space(SPACE_ID, "Test space", "Bloque A", capacity, UUID.randomUUID(), SpaceCategory.SPORTS);
    }

    private static SpaceSchedule schedule(LocalTime start, LocalTime end) {
        return new SpaceSchedule(UUID.randomUUID(), SPACE_ID, FUTURE_DATE.getDayOfWeek().getValue(), start, end);
    }

    private static Reservation activeReservation() {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                UUID.randomUUID(),
                SPACE_ID,
                FUTURE_DATE,
                BLOCK_START,
                BLOCK_END,
                LocalDateTime.now(),
                null,
                null,
                null);
    }
}
