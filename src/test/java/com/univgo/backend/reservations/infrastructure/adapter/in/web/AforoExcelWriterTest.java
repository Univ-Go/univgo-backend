package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.univgo.backend.reservations.application.port.in.GetSpaceAforoReportUseCase.AforoReport;
import com.univgo.backend.reservations.domain.BlockAforoRow;
import com.univgo.backend.reservations.domain.OccupantView;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

class AforoExcelWriterTest {

    @Test
    void namesTheFileAfterTheSpaceWithoutAccentsAndTheDay() {
        AforoReport report = new AforoReport("Sala de Estudio Nº 2", LocalDate.of(2026, 9, 23), List.of());

        assertThat(AforoExcelWriter.fileName(report, LocalDateTime.of(2026, 10, 7, 14, 30, 15)))
                .isEqualTo("aforo_sala-de-estudio-n-2_2026-09-23_20261007-143015.xlsx");
    }

    @Test
    void writesASummarySheetAndOneSheetPerBlockWithItsRoster() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 23);
        OccupantView ana = new OccupantView(
                "Ana Pérez", "1001", "Ingeniería", ReservationState.FINISHED, LocalDateTime.of(date, LocalTime.of(14, 5)));
        OccupantView noShow = new OccupantView("Luis Gómez", "1002", null, ReservationState.EXPIRED, null);
        AforoReport report = new AforoReport("Gimnasio", date, List.of(
                new BlockAforoRow(new TimeBlock(LocalTime.of(14, 0), LocalTime.of(16, 0)), 30, 2, 1, 1, 0, 30,
                        List.of(ana, noShow)),
                new BlockAforoRow(new TimeBlock(LocalTime.of(16, 0), LocalTime.of(18, 0)), 30, 0, 0, 0, 0, 30,
                        List.of())));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AforoExcelWriter.write(report, out);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);
            Sheet summary = workbook.getSheet("Resumen");
            assertThat(summary.getRow(3).getCell(0).getStringCellValue()).isEqualTo("Total");
            assertThat(summary.getRow(3).getCell(3).getNumericCellValue()).isEqualTo(60);

            Sheet block = workbook.getSheet("14.00 - 16.00");
            // 9 label/value rows, a blank one, the roster header, then the roster.
            assertThat(block.getRow(11).getCell(0).getStringCellValue()).isEqualTo("Ana Pérez");
            assertThat(block.getRow(11).getCell(3).getStringCellValue()).isEqualTo("14:05");
            assertThat(block.getRow(12).getCell(4).getStringCellValue()).isEqualTo("Expirada");
        }
    }
}
