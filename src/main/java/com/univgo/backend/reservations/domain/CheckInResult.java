package com.univgo.backend.reservations.domain;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record CheckInResult(
        CheckInVerdict verdict,
        String studentName,
        LocalDateTime blockEnd,
        LocalDateTime opensAt,
        LocalDateTime expiredAt,
        LocalDateTime checkedInAt,
        LocalTime otherBlockStart,
        LocalTime otherBlockEnd) {

    public static CheckInResult spaceClosed() {
        return new CheckInResult(CheckInVerdict.SPACE_CLOSED, null, null, null, null, null, null, null);
    }

    public static CheckInResult notExists() {
        return new CheckInResult(CheckInVerdict.NOT_EXISTS, null, null, null, null, null, null, null);
    }

    public static CheckInResult otherBlock(LocalTime actualStart, LocalTime actualEnd) {
        return new CheckInResult(CheckInVerdict.OTHER_BLOCK, null, null, null, null, null, actualStart, actualEnd);
    }

    public static CheckInResult tooEarly(LocalDateTime opensAt) {
        return new CheckInResult(CheckInVerdict.TOO_EARLY, null, null, opensAt, null, null, null, null);
    }

    public static CheckInResult expiredAlready(LocalDateTime expiredAt) {
        return new CheckInResult(CheckInVerdict.EXPIRED_ALREADY, null, null, null, expiredAt, null, null, null);
    }

    public static CheckInResult alreadyUsed(String studentName, LocalDateTime checkedInAt) {
        return new CheckInResult(CheckInVerdict.ALREADY_USED, studentName, null, null, null, checkedInAt, null, null);
    }

    public static CheckInResult valid(String studentName, LocalDateTime blockEnd, LocalDateTime checkedInAt) {
        return new CheckInResult(CheckInVerdict.VALID, studentName, blockEnd, null, null, checkedInAt, null, null);
    }
}
