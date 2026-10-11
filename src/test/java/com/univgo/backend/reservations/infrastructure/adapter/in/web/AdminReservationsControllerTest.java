package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase;
import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase.CreateReservationCommand;
import com.univgo.backend.reservations.application.port.in.GetAllReservationsUseCase;
import com.univgo.backend.reservations.application.port.in.GetInstitutionConfigUseCase;
import com.univgo.backend.reservations.domain.ConfirmationCodeGenerator;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.AdminCreateReservationRequest;
import com.univgo.backend.spaces.application.port.in.GetSpaceClosuresUseCase;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import com.univgo.backend.users.domain.UserNotFoundException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class AdminReservationsControllerTest {

    private static final UUID GYM_SPACE_ID = GymSpaceAccessGuard.GYM_SPACE_ID;
    private static final UUID OTHER_SPACE_ID = UUID.randomUUID();
    private static final UUID STUDENT_ID = UUID.randomUUID();

    @Mock
    private GetAllReservationsUseCase getAllReservationsUseCase;
    @Mock
    private GetInstitutionConfigUseCase getInstitutionConfigUseCase;
    @Mock
    private GetSpaceClosuresUseCase getSpaceClosuresUseCase;
    @Mock
    private CreateReservationUseCase createReservationUseCase;
    @Mock
    private UserRepositoryPort userRepositoryPort;

    private AdminReservationsController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminReservationsController(
                getAllReservationsUseCase,
                getInstitutionConfigUseCase,
                getSpaceClosuresUseCase,
                createReservationUseCase,
                userRepositoryPort);
    }

    @Test
    void resolvesStudentByIdentifierAndBooksForThem() {
        User student = new User(
                STUDENT_ID, "1234567890", "student@univgo.edu", "Student", "Name", "hash", Set.of("STUDENT"), null);
        when(userRepositoryPort.findByLoginIdentifier("1234567890")).thenReturn(Optional.of(student));
        Reservation reservation = reservationFor(STUDENT_ID, GYM_SPACE_ID);
        when(createReservationUseCase.execute(any())).thenReturn(reservation);
        when(getInstitutionConfigUseCase.execute()).thenReturn(new InstitutionConfig(120, 15, 75, 1));
        when(getSpaceClosuresUseCase.inForce()).thenReturn(List.of());

        var request = new AdminCreateReservationRequest(
                "1234567890", GYM_SPACE_ID, LocalDate.now(), LocalTime.of(14, 0));
        var response = controller.createForStudent(request, authFor("ROLE_ADMIN"));

        assertThat(response.userId()).isEqualTo(STUDENT_ID);
        verify(createReservationUseCase).execute(new CreateReservationCommand(
                STUDENT_ID, GYM_SPACE_ID, request.reservationDate(), request.startTime()));
    }

    @Test
    void unknownIdentifierIsRejectedWithoutBooking() {
        when(userRepositoryPort.findByLoginIdentifier("nobody")).thenReturn(Optional.empty());

        var request = new AdminCreateReservationRequest("nobody", GYM_SPACE_ID, LocalDate.now(), LocalTime.of(14, 0));

        assertThatThrownBy(() -> controller.createForStudent(request, authFor("ROLE_ADMIN")))
                .isInstanceOf(UserNotFoundException.class);
        verify(createReservationUseCase, never()).execute(any());
    }

    @Test
    void gymAdminCannotBookOutsideTheGymSpace() {
        var request = new AdminCreateReservationRequest(
                "1234567890", OTHER_SPACE_ID, LocalDate.now(), LocalTime.of(14, 0));

        assertThatThrownBy(() -> controller.createForStudent(request, authFor("ROLE_GYM_ADMIN")))
                .isInstanceOf(AccessDeniedException.class);
        verify(userRepositoryPort, never()).findByLoginIdentifier(any());
        verify(createReservationUseCase, never()).execute(any());
    }

    @Test
    void gymAdminCanBookForTheGymSpace() {
        User student = new User(
                STUDENT_ID, "1234567890", "student@univgo.edu", "Student", "Name", "hash", Set.of("STUDENT"), null);
        when(userRepositoryPort.findByLoginIdentifier("1234567890")).thenReturn(Optional.of(student));
        Reservation reservation = reservationFor(STUDENT_ID, GYM_SPACE_ID);
        when(createReservationUseCase.execute(any())).thenReturn(reservation);
        when(getInstitutionConfigUseCase.execute()).thenReturn(new InstitutionConfig(120, 15, 75, 1));
        when(getSpaceClosuresUseCase.inForce()).thenReturn(List.of());

        var request = new AdminCreateReservationRequest(
                "1234567890", GYM_SPACE_ID, LocalDate.now(), LocalTime.of(14, 0));
        var response = controller.createForStudent(request, authFor("ROLE_GYM_ADMIN"));

        assertThat(response.userId()).isEqualTo(STUDENT_ID);
    }

    private static Reservation reservationFor(UUID userId, UUID spaceId) {
        return new Reservation(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                ConfirmationCodeGenerator.generate(),
                userId,
                spaceId,
                LocalDate.now(),
                LocalTime.of(14, 0),
                LocalTime.of(16, 0),
                LocalDateTime.now(),
                null,
                null,
                null);
    }

    private static Authentication authFor(String authority) {
        return new UsernamePasswordAuthenticationToken(
                UUID.randomUUID().toString(), null, List.of(new SimpleGrantedAuthority(authority)));
    }
}
