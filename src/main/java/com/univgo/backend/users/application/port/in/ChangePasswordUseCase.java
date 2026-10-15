package com.univgo.backend.users.application.port.in;

import java.util.UUID;

public interface ChangePasswordUseCase {

    void execute(UUID userId, ChangePasswordCommand command);

    record ChangePasswordCommand(String currentPassword, String newPassword, String confirmation) {
    }
}
