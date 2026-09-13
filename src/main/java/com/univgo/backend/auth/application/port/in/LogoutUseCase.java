package com.univgo.backend.auth.application.port.in;

public interface LogoutUseCase {

    void execute(LogoutCommand command);

    record LogoutCommand(String refreshToken) {
    }
}
