package com.univgo.backend.shared.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * RFC 9562 UUIDv7: a 48-bit millisecond timestamp followed by random bits.
 * Unlike UUIDv4 ({@link UUID#randomUUID()}), values sort roughly by creation
 * time — new rows land at the end of a B-tree index instead of scattering
 * writes across it, and ids are naturally filterable/orderable like a ULID.
 *
 * <p>Postgres doesn't need a specific version for this — the column stays a
 * plain {@code UUID}; only the generation side changes, so it works against
 * any Postgres version (including managed providers that don't yet offer the
 * native {@code uuidv7()} builtin from PG18).
 */
public final class Uuidv7Generator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Uuidv7Generator() {
    }

    public static UUID generate() {
        long timestampMs = Instant.now().toEpochMilli();
        byte[] value = new byte[16];

        value[0] = (byte) (timestampMs >>> 40);
        value[1] = (byte) (timestampMs >>> 32);
        value[2] = (byte) (timestampMs >>> 24);
        value[3] = (byte) (timestampMs >>> 16);
        value[4] = (byte) (timestampMs >>> 8);
        value[5] = (byte) timestampMs;

        byte[] random = new byte[10];
        RANDOM.nextBytes(random);
        System.arraycopy(random, 0, value, 6, 10);

        value[6] = (byte) (0x70 | (value[6] & 0x0F)); // version nibble: 0111
        value[8] = (byte) (0x80 | (value[8] & 0x3F)); // variant bits: 10

        long msb = 0;
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (value[i] & 0xFF);
        }
        long lsb = 0;
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (value[i] & 0xFF);
        }

        return new UUID(msb, lsb);
    }
}
