package com.univgo.backend.users.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.univgo.backend.users.application.port.in.ChangePasswordUseCase.ChangePasswordCommand;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.InvalidPasswordChangeException;
import com.univgo.backend.users.domain.User;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ChangePasswordServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String CURRENT = "old-secret";

    // A real encoder: the point is that the stored value is a hash that matches, not a mock's echo.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private ChangePasswordService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new ChangePasswordService(userRepositoryPort, passwordEncoder);
        user = new User(USER_ID, "123", "a@uni.edu", "Ana", "Ruiz",
                passwordEncoder.encode(CURRENT), Set.of("STUDENT"), null);
        when(userRepositoryPort.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    @Test
    void storesTheNewHash() {
        service.execute(USER_ID, new ChangePasswordCommand(CURRENT, "new-secret", "new-secret"));

        assertThat(passwordEncoder.matches("new-secret", user.getPassword())).isTrue();
        verify(userRepositoryPort).save(user);
    }

    @Test
    void rejectsAWrongCurrentPassword() {
        assertRefused(new ChangePasswordCommand("wrong", "new-secret", "new-secret"), "incorrect");
    }

    private void assertRefused(ChangePasswordCommand command, String messagePart) {
        assertThatThrownBy(() -> service.execute(USER_ID, command))
                .isInstanceOf(InvalidPasswordChangeException.class)
                .hasMessageContaining(messagePart);
        verify(userRepositoryPort, never()).save(any());
    }
}
