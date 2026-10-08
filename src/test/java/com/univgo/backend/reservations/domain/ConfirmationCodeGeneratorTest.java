package com.univgo.backend.reservations.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConfirmationCodeGeneratorTest {

    // Small numbers are the ones that would lose digits: 4217 must still read "004217".
    @Test
    void alwaysSixDigitsEvenForSmallNumbers() {
        for (int i = 0; i < 10_000; i++) {
            assertThat(ConfirmationCodeGenerator.generate()).matches("\\d{6}");
        }
    }
}
