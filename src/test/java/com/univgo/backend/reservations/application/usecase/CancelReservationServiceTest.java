package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.CancelledBy;
import com.univgo.backend.reservations.domain.CannotCancelInProgressReservationException;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationCheckpoint;
import com.univgo.backend.reservations.domain.ReservationNotFoundException;
import com.univgo.backend.reservations.domain.ReservationSchedule;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelReservationServiceTest {

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    @Spy
    private Clock clock = Clock.systemDefaultZone();

    @InjectMocks
    private CancelReservationService service;

    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    void ownerCanCancelBeforeTheBlockStarts() {
        UUID userId = UUID.randomUUID();
        Reservation reservation = reservation(userId);
        when(reservationRepositoryPort.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(reservationRepositoryPort.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = service.execute(reservation.getId(), userId, false);

        assertThat(result.getCancelledAt()).isNotNull();
        assertThat(result.getCancelledBy()).isEqualTo(CancelledBy.STUDENT);
    }

    @Test
    void adminCanCancelAnyonesReservation() {
        UUID ownerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Reservation reservation = reservation(ownerId);
        when(reservationRepositoryPort.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(reservationRepositoryPort.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = service.execute(reservation.getId(), adminId, true);

        assertThat(result.getCancelledBy()).isEqualTo(CancelledBy.ADMIN);
    }

    @Test
    void anotherStudentCannotCancelSomeoneElsesReservation() {
        UUID ownerId = UUID.randomUUID();
        UUID otherStudentId = UUID.randomUUID();
        Reservation reservation = reservation(ownerId);
        when(reservationRepositoryPort.findById(reservation.getId())).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.execute(reservation.getId(), otherStudentId, false))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void cancellingAnUnknownReservationThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(reservationRepositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(id, UUID.randomUUID(), false))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void cannotCancelWhileInProgressEvenAsAdmin() {
        UUID ownerId = UUID.randomUUID();
        Reservation reservation = reservation(ownerId);
        reservation.checkIn(LocalDateTime.of(FUTURE_DATE, LocalTime.of(14, 0)));
        when(reservationRepositoryPort.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);

        assertThatThrownBy(() -> service.execute(reservation.getId(), UUID.randomUUID(), true))
                .isInstanceOf(CannotCancelInProgressReservationException.class);
    }

    private static Reservation reservation(UUID ownerId) {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                ownerId,
                UUID.randomUUID(),
                new ReservationSchedule(FUTURE_DATE, new TimeBlock(LocalTime.of(14, 0), LocalTime.of(16, 0))),
                LocalDateTime.now(),
                ReservationCheckpoint.initial());
    }
}
