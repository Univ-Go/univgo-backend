package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
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
class CancelSpaceReservationsServiceTest {

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    @InjectMocks
    private CancelSpaceReservationsService service;

    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    private static final UUID SPACE_ID = UUID.randomUUID();
    // Far enough ahead that a fresh reservation is unambiguously RESERVED, not EXPIRED.
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @Test
    void cancelsOnlyStillReservedReservationsAndSkipsInProgressAndAlreadyTerminalOnes() {
        Reservation stillReserved = reservation();
        Reservation inProgress = reservation();
        inProgress.checkIn(LocalDateTime.now());

        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(reservationRepositoryPort.findActiveBySpaceId(SPACE_ID)).thenReturn(List.of(stillReserved, inProgress));

        int cancelled = service.execute(SPACE_ID);

        assertThat(cancelled).isEqualTo(1);
        assertThat(stillReserved.getCancelledAt()).isNotNull();
        assertThat(inProgress.getCheckedInAt()).isNotNull();
        assertThat(inProgress.getCancelledAt()).isNull();
        verify(reservationRepositoryPort, times(1)).save(any());
    }

    @Test
    void returnsZeroWhenNothingToCancel() {
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(reservationRepositoryPort.findActiveBySpaceId(SPACE_ID)).thenReturn(List.of());

        assertThat(service.execute(SPACE_ID)).isZero();
    }

    private static Reservation reservation() {
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
