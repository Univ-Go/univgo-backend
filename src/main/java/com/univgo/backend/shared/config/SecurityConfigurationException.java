package com.univgo.backend.shared.config;

/** Wraps the checked {@code Exception} the security DSL declares, which only ever fires at boot. */
public class SecurityConfigurationException extends RuntimeException {

    public SecurityConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
