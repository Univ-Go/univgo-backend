package com.univgo.backend.auth.domain;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid identification or password");
    }
}
