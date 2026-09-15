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
    void sortsRoughlyByCreationTimeUnlikeRandomUuids() throws InterruptedException {
        UUID first = Uuidv7Generator.generate();
        Thread.sleep(5);
        UUID second = Uuidv7Generator.generate();

        assertThat(first).isLessThan(second);
    }
}
