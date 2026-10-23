package com.univgo.backend.users.domain;

/**
 * Every way a password change can be refused: wrong current password, a new one too short, or a
 * confirmation that doesn't match. Mapped to 400, not 401 — a typo here must not read to the client
 * as an expired session and log the student out.
 */
public class InvalidPasswordChangeException extends RuntimeException {

    public InvalidPasswordChangeException(String message) {
        super(message);
    }
}
