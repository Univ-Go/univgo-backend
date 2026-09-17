package com.univgo.backend.auth.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.auth.application.port.in.LoginUseCase.LoginCommand;
import com.univgo.backend.auth.application.port.in.LoginUseCase.LoginResult;
import com.univgo.backend.auth.application.port.out.RefreshTokenRepositoryPort;
import com.univgo.backend.auth.application.port.out.TokenProviderPort;
import com.univgo.backend.auth.domain.InvalidCredentialsException;
import com.univgo.backend.auth.domain.RefreshToken;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final String PASSWORD = "Contrasena123!";
    private static final User USER = new User(
            UUID.randomUUID(), "1234567890", "sofia.ramirez@univgo.edu", "Sofía", "Ramírez", "hash",
            Set.of("STUDENT"));

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenProviderPort tokenProviderPort;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    @InjectMocks
    private LoginService service;

    @ParameterizedTest
    @ValueSource(strings = {"1234567890", "sofia.ramirez@univgo.edu"})
    void signsInWithEitherIdentifier(String identifier) {
        when(userRepositoryPort.findByLoginIdentifier(identifier)).thenReturn(Optional.of(USER));
        when(passwordEncoder.matches(PASSWORD, "hash")).thenReturn(true);
        when(tokenProviderPort.generateAccessToken(USER)).thenReturn("access");
        when(tokenProviderPort.generateRefreshToken(USER))
                .thenReturn(new TokenProviderPort.IssuedToken("refresh", Instant.now().plusSeconds(1800)));
        when(tokenProviderPort.getAccessExpirationSeconds()).thenReturn(900L);

        LoginResult result = service.execute(new LoginCommand(identifier, PASSWORD));

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.refreshToken()).isEqualTo("refresh");
        assertThat(result.user()).isSameAs(USER);
        verify(refreshTokenRepositoryPort).save(any(RefreshToken.class));
    }

    @Test
    void unknownIdentifierIsRejectedWithoutStoringAnything() {
        when(userRepositoryPort.findByLoginIdentifier("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new LoginCommand("nobody", PASSWORD)))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(refreshTokenRepositoryPort, never()).save(any());
    }

    @Test
    void wrongPasswordIsRejectedWithTheSameErrorAsAnUnknownUser() {
        when(userRepositoryPort.findByLoginIdentifier("1234567890")).thenReturn(Optional.of(USER));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new LoginCommand("1234567890", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(refreshTokenRepositoryPort, never()).save(any());
    }
}
