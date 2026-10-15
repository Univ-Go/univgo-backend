package com.univgo.backend.users.application.usecase;

import com.univgo.backend.users.application.port.in.ChangePasswordUseCase;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.application.port.out.UserSessionRevokerPort;
import com.univgo.backend.users.domain.InvalidPasswordChangeException;
import com.univgo.backend.users.domain.User;
import com.univgo.backend.users.domain.UserNotFoundException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePasswordService implements ChangePasswordUseCase {

    static final int MIN_LENGTH = 8;

    private final UserRepositoryPort userRepositoryPort;
    private final UserSessionRevokerPort userSessionRevokerPort;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordService(
            UserRepositoryPort userRepositoryPort,
            UserSessionRevokerPort userSessionRevokerPort,
            PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.userSessionRevokerPort = userSessionRevokerPort;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Transactional because the new hash and the revoked sessions are only correct together: a new
     * password with old sessions still alive defeats the point of logging everyone out. Access
     * tokens already issued stay valid until they expire (15 min) — accepted, not overlooked.
     */
    @Override
    @Transactional
    public void execute(UUID userId, ChangePasswordCommand command) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (command.currentPassword() == null
                || !passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordChangeException("Current password is incorrect");
        }
        if (command.newPassword() == null || command.newPassword().length() < MIN_LENGTH) {
            throw new InvalidPasswordChangeException(
                    "New password must be at least " + MIN_LENGTH + " characters");
        }
        if (!command.newPassword().equals(command.confirmation())) {
            throw new InvalidPasswordChangeException("Password confirmation does not match");
        }

        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepositoryPort.save(user);
        userSessionRevokerPort.revokeAllSessions(userId);
    }
}
