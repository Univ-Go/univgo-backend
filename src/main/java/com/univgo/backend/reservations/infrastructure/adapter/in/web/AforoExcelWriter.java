package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.GetSpaceAforoReportUseCase.AforoReport;
import com.univgo.backend.reservations.domain.BlockAforoRow;
import com.univgo.backend.reservations.domain.OccupantView;
import com.univgo.backend.reservations.domain.ReservationState;
import com.univgo.backend.spaces.domain.TimeBlock;
import java.io.IOException;
import java.io.OutputStream;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** A "Resumen" sheet with one line per block and the day's totals, then one sheet per block with its roster. */
final class AforoExcelWriter {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] SUMMARY_HEADERS = {
        "Fecha", "Espacio", "Horario", "Aforo total", "Reservas creadas",
        "Check-ins confirmados", "Expiradas", "Canceladas", "Cupos libres"
    };

    // The numeric summary columns, in header order from "Aforo total" on; the totals row sums the same list.
    private static final List<ToIntFunction<BlockAforoRow>> COUNTS = List.of(
            BlockAforoRow::capacity,
            BlockAforoRow::created,
            BlockAforoRow::checkedIn,
            BlockAforoRow::expired,
            BlockAforoRow::cancelled,
            BlockAforoRow::free);

    private static final String[] ROSTER_HEADERS = {"Nombre", "Documento", "Escuela", "Hora de check-in", "Estado"};

    private AforoExcelWriter() {
    }

    static void write(AforoReport report, OutputStream out) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Styles styles = new Styles(workbook);
            writeSummary(workbook.createSheet("Resumen"), report, styles);
            for (BlockAforoRow row : report.rows()) {
                // ':' is not allowed in a sheet name.
                String name = row.block().start().format(DateTimeFormatter.ofPattern("HH.mm"))
                        + " - " + row.block().end().format(DateTimeFormatter.ofPattern("HH.mm"));
                writeBlock(workbook.createSheet(name), report, row, styles);
            }
            workbook.write(out);
        }
    }

    /**
     * {@code aforo_sala-estudio-2_2026-09-23_20261007-143015.xlsx}: the download's own date and time
     * at the end, so no two exports share a name. No ':' anywhere — Windows forbids it.
     */
    static String fileName(AforoReport report) {
        return "aforo_" + slug(report.spaceName()) + "_" + report.date()
                + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".xlsx";
    }

    /** "Sala Estudio 2" → "sala-estudio-2"; accents dropped so the name survives any file system. */
    private static String slug(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private static void writeSummary(Sheet sheet, AforoReport report, Styles styles) {
        // ponytail: fixed width, autoSizeColumn needs AWT fonts that a headless server may lack.
        sheet.setDefaultColumnWidth(22);
        header(sheet.createRow(0), SUMMARY_HEADERS, styles);
        sheet.createFreezePane(0, 1);

        int r = 1;
        for (BlockAforoRow row : report.rows()) {
            CellStyle style = styles.body(r);
            Row line = sheet.createRow(r++);
            cell(line, 0, report.date().toString(), style);
            cell(line, 1, report.spaceName(), style);
            cell(line, 2, horario(row.block()), style);
            for (int c = 0; c < COUNTS.size(); c++) {
                cell(line, 3 + c, COUNTS.get(c).applyAsInt(row), style);
            }
        }

        Row totals = sheet.createRow(r);
        cell(totals, 0, "Total", styles.total);
        cell(totals, 1, "", styles.total);
        cell(totals, 2, "", styles.total);
        for (int c = 0; c < COUNTS.size(); c++) {
            cell(totals, 3 + c, report.rows().stream().mapToInt(COUNTS.get(c)).sum(), styles.total);
        }
    }

    /** The block's figures as label/value pairs on top, then who booked it. */
    private static void writeBlock(Sheet sheet, AforoReport report, BlockAforoRow row, Styles styles) {
        sheet.setDefaultColumnWidth(22);
        Object[] values = {
            report.date().toString(), report.spaceName(), horario(row.block()),
            row.capacity(), row.created(), row.checkedIn(), row.expired(), row.cancelled(), row.free()
        };
        for (int i = 0; i < SUMMARY_HEADERS.length; i++) {
            Row line = sheet.createRow(i);
            cell(line, 0, SUMMARY_HEADERS[i], styles.label);
            cell(line, 1, values[i], styles.cell);
        }

        int r = SUMMARY_HEADERS.length + 1;
        header(sheet.createRow(r++), ROSTER_HEADERS, styles);
        for (OccupantView occupant : row.roster()) {
            CellStyle style = styles.body(r);
            Row line = sheet.createRow(r++);
            cell(line, 0, occupant.studentName(), style);
            cell(line, 1, occupant.document(), style);
            cell(line, 2, occupant.school(), style);
            cell(line, 3, occupant.checkedInAt() == null ? "" : TIME.format(occupant.checkedInAt()), style);
            cell(line, 4, estado(occupant.state()), styles.status.get(occupant.state()));
        }
    }

    private static void header(Row row, String[] headers, Styles styles) {
        for (int i = 0; i < headers.length; i++) {
            cell(row, i, headers[i], styles.header);
        }
    }

    private static void cell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof Integer n) {
            cell.setCellValue(n);
        } else {
            cell.setCellValue(value == null ? "" : value.toString());
        }
        cell.setCellStyle(style);
    }

    private static String horario(TimeBlock block) {
        return TIME.format(block.start()) + "–" + TIME.format(block.end());
    }

    private static String estado(ReservationState state) {
        return switch (state) {
            case RESERVED -> "Reservada";
            case SUSPENDED -> "Suspendida";
            case IN_PROGRESS -> "En curso";
            case FINISHED -> "Finalizada";
            case EXPIRED -> "Expirada";
            case CANCELLED -> "Cancelada";
        };
    }

    /**
     * Colours from the frontend theme (univgo-frontend {@code _univgo-theme.scss}, light mode). The
     * pale status fills are the theme's {@code status-*-pale} rgba values flattened over white, since
     * a cell fill has no transparency. Built once per workbook: a file holds at most 64 000 styles.
     */
    private static final class Styles {

        private static final String BRAND_800 = "7f0013";
        private static final String BRAND_50 = "fff1f0";
        private static final String NEUTRAL_0 = "ffffff";
        private static final String NEUTRAL_50 = "f3f4f5";
        private static final String NEUTRAL_100 = "edeeef";
        private static final String NEUTRAL_200 = "e1e3e4";
        private static final String NEUTRAL_600 = "7a5f5d";
        private static final String NEUTRAL_900 = "191c1d";

        final CellStyle header;
        final CellStyle cell;
        final CellStyle zebra;
        final CellStyle label;
        final CellStyle total;
        final Map<ReservationState, CellStyle> status = new EnumMap<>(ReservationState.class);

        Styles(XSSFWorkbook workbook) {
            header = style(workbook, BRAND_800, NEUTRAL_0, true);
            cell = style(workbook, NEUTRAL_0, NEUTRAL_900, false);
            zebra = style(workbook, NEUTRAL_50, NEUTRAL_900, false);
            label = style(workbook, NEUTRAL_100, NEUTRAL_900, true);
            total = style(workbook, BRAND_50, BRAND_800, true);
            total.setBorderTop(BorderStyle.MEDIUM);
            ((XSSFCellStyle) total).setTopBorderColor(color(BRAND_800));

            status.put(ReservationState.RESERVED, style(workbook, "e9effa", "2563c9", true)); // info
            status.put(ReservationState.IN_PROGRESS, style(workbook, "e6f7f5", "00504c", true)); // teal
            status.put(ReservationState.FINISHED, style(workbook, "e6f7f5", "00504c", true)); // teal
            status.put(ReservationState.EXPIRED, style(workbook, "f3ebe0", "9a5b00", true)); // warning
            status.put(ReservationState.CANCELLED, style(workbook, "faebe5", "cc3300", true)); // danger
            status.put(ReservationState.SUSPENDED, style(workbook, NEUTRAL_100, NEUTRAL_600, true));
        }

        /** Alternate row shading, counted from the sheet's first row. */
        CellStyle body(int rowIndex) {
            return rowIndex % 2 == 0 ? zebra : cell;
        }

        private static XSSFCellStyle style(XSSFWorkbook workbook, String fill, String ink, boolean bold) {
            XSSFCellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(color(fill));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            XSSFFont font = workbook.createFont();
            font.setBold(bold);
            font.setColor(color(ink));
            style.setFont(font);

            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            XSSFColor border = color(NEUTRAL_200);
            style.setTopBorderColor(border);
            style.setBottomBorderColor(border);
            style.setLeftBorderColor(border);
            style.setRightBorderColor(border);
            return style;
        }

        private static XSSFColor color(String hex) {
            return new XSSFColor(HexFormat.of().parseHex(hex));
        }
    }
}
