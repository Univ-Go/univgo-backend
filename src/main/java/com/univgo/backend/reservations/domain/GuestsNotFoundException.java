package com.univgo.backend.reservations.domain;

import java.util.List;

public class GuestsNotFoundException extends RuntimeException {

    public GuestsNotFoundException(List<String> missingIdentifications) {
        super("The following guests are not part of the University community: "
                + String.join(", ", missingIdentifications));
    }
}
