package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.GetSpaceAforoReportUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceAforoReportUseCase.AforoReport;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/admin/spaces")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAforoExportController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final GetSpaceAforoReportUseCase getSpaceAforoReportUseCase;
    private final Clock clock;

    public AdminAforoExportController(GetSpaceAforoReportUseCase getSpaceAforoReportUseCase, Clock clock) {
        this.getSpaceAforoReportUseCase = getSpaceAforoReportUseCase;
        this.clock = clock;
    }

    /**
     * The report is built here, before the response starts, so a bad date or unknown space is still a
     * plain 400/404; only writing the workbook runs asynchronously, off the request thread.
     */
    @GetMapping("/{spaceId}/blocks/export")
    public ResponseEntity<StreamingResponseBody> exportBlocks(
            @PathVariable UUID spaceId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        AforoReport report = getSpaceAforoReportUseCase.execute(spaceId, date);
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(AforoExcelWriter.fileName(report, LocalDateTime.now(clock)), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(out -> AforoExcelWriter.write(report, out));
    }
}
