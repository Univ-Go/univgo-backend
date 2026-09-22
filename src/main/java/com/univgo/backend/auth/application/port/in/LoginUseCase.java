package com.univgo.backend.auth.application.port.in;

import com.univgo.backend.users.domain.User;

public interface LoginUseCase {

    LoginResult execute(LoginCommand command);

    /** {@code identifier} is the ID number or the institutional email; the two share one field. */
    record LoginCommand(String identifier, String password) {
    }

    /**
     * Carries the authenticated user so the web adapter can describe the session without a second
     * lookup: the tokens become cookies and never reach the response body.
     */
    record LoginResult(String accessToken, String refreshToken, long expiresIn, User user) {
    }
}
