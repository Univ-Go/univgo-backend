package com.univgo.backend.reservations.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.in.CheckInReservationUseCase.CheckInCommand;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.application.port.out.ReservationRepositoryPort;
import com.univgo.backend.reservations.domain.CancelledBy;
import com.univgo.backend.reservations.domain.CheckInResult;
import com.univgo.backend.reservations.domain.CheckInVerdict;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckInReservationServiceTest {

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    @InjectMocks
    private CheckInReservationService service;

    private static final InstitutionConfig CONFIG = new InstitutionConfig(120, 15, 75, 1);
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);
    private static final String CODE = "some-qr-code";
    private static final UUID SPACE_ID = UUID.randomUUID();
    private static final UUID OTHER_SPACE_ID = UUID.randomUUID();

    @Test
    void unknownCodeIsNotExists() {
        when(reservationRepositoryPort.findByQrCodeData(CODE)).thenReturn(Optional.empty());

        CheckInResult result = service.execute(new CheckInCommand(CODE, SPACE_ID, null, null));

        assertThat(result.verdict()).isEqualTo(CheckInVerdict.NOT_EXISTS);
    }

    @Test
    void cancelledReservationIsNotExists() {
        Reservation reservation = reservationOn(FUTURE_DATE, LocalTime.of(14, 0), LocalTime.of(16, 0), LocalDateTime.now());
        reservation.cancel(CancelledBy.STUDENT, LocalDateTime.now(), CONFIG.tolerance(), CONFIG.minUsage());
        when(reservationRepositoryPort.findByQrCodeData(CODE)).thenReturn(Optional.of(reservation));

        CheckInResult result = service.execute(new CheckInCommand(CODE, SPACE_ID, null, null));

        assertThat(result.verdict()).isEqualTo(CheckInVerdict.NOT_EXISTS);
    }

    @Test
    void mismatchedSpaceIsOtherBlock() {
        Reservation reservation = reservationOn(FUTURE_DATE, LocalTime.of(14, 0), LocalTime.of(16, 0), LocalDateTime.now());
        when(reservationRepositoryPort.findByQrCodeData(CODE)).thenReturn(Optional.of(reservation));

        CheckInResult result = service.execute(new CheckInCommand(CODE, OTHER_SPACE_ID, null, null));

        assertThat(result.verdict()).isEqualTo(CheckInVerdict.OTHER_BLOCK);
        assertThat(result.otherBlockStart()).isEqualTo(LocalTime.of(14, 0));
        assertThat(result.otherBlockEnd()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    void mismatchedExpectedBlockIsOtherBlock() {
        Reservation reservation = reservationOn(FUTURE_DATE, LocalTime.of(14, 0), LocalTime.of(16, 0), LocalDateTime.now());
        when(reservationRepositoryPort.findByQrCodeData(CODE)).thenReturn(Optional.of(reservation));

        CheckInResult result = service.execute(new CheckInCommand(CODE, SPACE_ID, LocalTime.of(16, 0), LocalTime.of(18, 0)));

        assertThat(result.verdict()).isEqualTo(CheckInVerdict.OTHER_BLOCK);
        assertThat(result.otherBlockStart()).isEqualTo(LocalTime.of(14, 0));
        assertThat(result.otherBlockEnd()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    void tooEarlyWhenScannedBeforeTheWindowOpens() {
        // Booked for a block a week from now: the check-in window hasn't opened yet no matter when this runs.
        Reservation reservation = reservationOn(FUTURE_DATE, LocalTime.of(14, 0), LocalTime.of(16, 0), LocalDateTime.now());
        when(reservationRepositoryPort.findByQrCodeData(CODE)).thenReturn(Optional.of(reservation));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);

        CheckInResult result = service.execute(new CheckInCommand(CODE, SPACE_ID, null, null));

        assertThat(result.verdict()).isEqualTo(CheckInVerdict.TOO_EARLY);
        assertThat(result.opensAt()).isNotNull();
    }

    @Test
    void expiredAlreadyWhenScannedPastTheCloseWithoutCheckingIn() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Reservation reservation = reservationOn(
                yesterday, LocalTime.of(14, 0), LocalTime.of(16, 0), LocalDateTime.of(yesterday.minusDays(1), LocalTime.of(9, 0)));
        when(reservationRepositoryPort.findByQrCodeData(CODE)).thenReturn(Optional.of(reservation));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);

        CheckInResult result = service.execute(new CheckInCommand(CODE, SPACE_ID, null, null));

        assertThat(result.verdict()).isEqualTo(CheckInVerdict.EXPIRED_ALREADY);
        verify(reservationRepositoryPort, never()).save(any());
    }

    @Test
    void alreadyUsedWhenAlreadyCheckedIn() {
        Reservation reservation = reservationOn(FUTURE_DATE, LocalTime.of(14, 0), LocalTime.of(16, 0), LocalDateTime.now());
        reservation.checkIn(LocalDateTime.now());
        when(reservationRepositoryPort.findByQrCodeData(CODE)).thenReturn(Optional.of(reservation));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(userRepositoryPort.findById(reservation.getUserId())).thenReturn(Optional.of(student()));

        CheckInResult result = service.execute(new CheckInCommand(CODE, SPACE_ID, null, null));

        assertThat(result.verdict()).isEqualTo(CheckInVerdict.ALREADY_USED);
    }

    @Test
    void validScanChecksInAStillReservedReservation() {
        // Last-minute-style reservation: block already "started" a minute ago, created just now,
        // so the window is open right now regardless of wall-clock time.
        LocalDateTime now = LocalDateTime.now();
        LocalTime blockStart = now.toLocalTime().minusMinutes(1);
        LocalTime blockEnd = blockStart.plusMinutes(120);
        Reservation reservation = reservationOn(now.toLocalDate(), blockStart, blockEnd, now);
        when(reservationRepositoryPort.findByQrCodeData(CODE)).thenReturn(Optional.of(reservation));
        when(institutionConfigRepositoryPort.getCurrent()).thenReturn(CONFIG);
        when(userRepositoryPort.findById(reservation.getUserId())).thenReturn(Optional.of(student()));

        CheckInResult result = service.execute(new CheckInCommand(CODE, SPACE_ID, null, null));

        assertThat(result.verdict()).isEqualTo(CheckInVerdict.VALID);
        assertThat(result.studentName()).isEqualTo("Ada Lovelace");
        assertThat(reservation.getCheckedInAt()).isNotNull();
        verify(reservationRepositoryPort).save(reservation);
    }

    private static Reservation reservationOn(LocalDate date, LocalTime start, LocalTime end, LocalDateTime createdAt) {
        return new Reservation(
                UUID.randomUUID(), CODE, UUID.randomUUID(), SPACE_ID, date, start, end, createdAt, null, null, null);
    }

    private static User student() {
        return new User(
                UUID.randomUUID(), "123", "ada@univgo.edu", "Ada", "Lovelace", "hash", Set.of("STUDENT"),
                "Facultad de Ingeniería");
    }
}
