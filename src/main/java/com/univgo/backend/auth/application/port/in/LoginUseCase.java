package com.univgo.backend.auth.application.port.in;

public interface LoginUseCase {

    LoginResult execute(LoginCommand command);

    record LoginCommand(String identification, String password) {
    }

    record LoginResult(String accessToken, long expiresIn) {
    }
}
