package com.univgo.backend.users.application.usecase;

import com.univgo.backend.users.application.port.in.ChangePasswordUseCase;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.InvalidPasswordChangeException;
import com.univgo.backend.users.domain.User;
import com.univgo.backend.users.domain.UserNotFoundException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void execute(UUID userId, ChangePasswordCommand command) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (command.currentPassword() == null
                || !passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordChangeException("Current password is incorrect");
        }

        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepositoryPort.save(user);
    }
}
