package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.CheckInResult;
import com.univgo.backend.reservations.domain.CheckInVerdict;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record CheckInScanResponse(
        CheckInVerdict verdict,
        String studentName,
        LocalDateTime blockEnd,
        LocalDateTime opensAt,
        LocalDateTime expiredAt,
        LocalDateTime checkedInAt,
        LocalTime otherBlockStart,
        LocalTime otherBlockEnd) {

    public static CheckInScanResponse from(CheckInResult result) {
        return new CheckInScanResponse(
                result.verdict(),
                result.studentName(),
                result.blockEnd(),
                result.opensAt(),
                result.expiredAt(),
                result.checkedInAt(),
                result.otherBlockStart(),
                result.otherBlockEnd());
    }
}
