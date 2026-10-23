package com.univgo.backend.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class Uuidv7GeneratorTest {

    @Test
    void generatesVersionSevenVariantTwoUuids() {
        UUID uuid = Uuidv7Generator.generate();

        assertThat(uuid.version()).isEqualTo(7);
        assertThat(uuid.variant()).isEqualTo(2);
    }

    @Test
    void generatesUniqueValues() {
        Set<UUID> generated = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            generated.add(Uuidv7Generator.generate());
        }

        assertThat(generated).hasSize(10_000);
    }

    @Test
    void sortsRoughlyByCreationTimeUnlikeRandomUuids() {
        UUID first = Uuidv7Generator.generate();

        // No sleep: busy-wait for the embedded millisecond itself to advance. Racing against
        // System.currentTimeMillis() instead of the UUID's own timestamp would occasionally keep a
        // "second" generated the instant before the tick, landing in the same millisecond as
        // "first" — the generator has no monotonic counter, so same-millisecond values can sort
        // either way on their random tail.
        UUID second;
        do {
            second = Uuidv7Generator.generate();
        } while (embeddedTimestamp(second) == embeddedTimestamp(first));

        assertThat(first).isLessThan(second);
    }

    private static long embeddedTimestamp(UUID uuid) {
        return uuid.getMostSignificantBits() >>> 16;
    }
}
