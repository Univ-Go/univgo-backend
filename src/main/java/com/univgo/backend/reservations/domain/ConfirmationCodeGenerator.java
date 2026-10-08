package com.univgo.backend.reservations.domain;

import java.security.SecureRandom;

/**
 * The 6-digit code an admin types when the QR does not scan. SecureRandom so that one student's
 * code says nothing about the next one's.
 */
public final class ConfirmationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private ConfirmationCodeGenerator() {
    }

    public static String generate() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
