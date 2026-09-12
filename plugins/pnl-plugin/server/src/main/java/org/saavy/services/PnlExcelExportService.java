package org.saavy.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.saavy.dto.PnlReportMatrixDTO;
import org.saavy.dto.PnlReportMatrixDTO.PnlReportRowDTO;
import org.saavy.dto.PnlReportMatrixDTO.PnlReportSectionDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PnlExcelExportService {

    public byte[] generateExcelReport(PnlReportMatrixDTO matrix) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("P&L Statement " + matrix.getYear());
            sheet.setDisplayGridlines(true);

            String currencyCode = matrix.getCurrency() != null ? matrix.getCurrency().trim().toUpperCase() : "PHP";
            String currSymbol = "PHP".equals(currencyCode) ? "\u20B1" : "EUR".equals(currencyCode) ? "\u20AC" : "$";
            String currencyFormatPattern;
            if ("PHP".equals(currencyCode)) {
                currencyFormatPattern = "[$PHP] #,##0.00;[Red]([$PHP] #,##0.00);\"-\"";
            } else if ("EUR".equals(currencyCode)) {
                currencyFormatPattern = "[$EUR] #,##0.00;[Red]([$EUR] #,##0.00);\"-\"";
            } else if ("GBP".equals(currencyCode)) {
                currencyFormatPattern = "[$GBP] #,##0.00;[Red]([$GBP] #,##0.00);\"-\"";
            } else if ("JPY".equals(currencyCode)) {
                currencyFormatPattern = "[$JPY] #,##0;[Red]([$JPY] #,##0);\"-\"";
            } else {
                currencyFormatPattern = "$#,##0.00;[Red]($#,##0.00);\"-\"";
            }

            DataFormat df = workbook.createDataFormat();
            short currencyFormat = df.getFormat(currencyFormatPattern);
            short percentFormat = df.getFormat("0.0%");

            // Fonts
            Font titleFont = workbook.createFont();
            titleFont.setFontName("Calibri");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());

            Font subTitleFont = workbook.createFont();
            subTitleFont.setFontName("Calibri");
            subTitleFont.setFontHeightInPoints((short) 10);
            subTitleFont.setItalic(true);
            subTitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());

            Font headerFont = workbook.createFont();
            headerFont.setFontName("Calibri");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            Font boldFont = workbook.createFont();
            boldFont.setFontName("Calibri");
            boldFont.setFontHeightInPoints((short) 11);
            boldFont.setBold(true);

            Font regularFont = workbook.createFont();
            regularFont.setFontName("Calibri");
            regularFont.setFontHeightInPoints((short) 11);

            // Header Style (Navy fill, white bold text)
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(headerStyle, BorderStyle.THIN);

            // Section Header Style (Light gray / Slate fill)
            CellStyle sectionStyle = workbook.createCellStyle();
            sectionStyle.setFont(boldFont);
            sectionStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            sectionStyle.setAlignment(HorizontalAlignment.LEFT);
            sectionStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorders(sectionStyle, BorderStyle.THIN);

            // Item Styles
            CellStyle itemTextStyle = workbook.createCellStyle();
            itemTextStyle.setFont(regularFont);
            setBorders(itemTextStyle, BorderStyle.THIN);

            CellStyle itemNumStyle = workbook.createCellStyle();
            itemNumStyle.setFont(regularFont);
            itemNumStyle.setDataFormat(currencyFormat);
            itemNumStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorders(itemNumStyle, BorderStyle.THIN);

            // Subtotal / Highlight Styles
            CellStyle subtotalTextStyle = workbook.createCellStyle();
            subtotalTextStyle.setFont(boldFont);
            subtotalTextStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            subtotalTextStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(subtotalTextStyle, BorderStyle.THIN);

            CellStyle subtotalNumStyle = workbook.createCellStyle();
            subtotalNumStyle.setFont(boldFont);
            subtotalNumStyle.setDataFormat(currencyFormat);
            subtotalNumStyle.setAlignment(HorizontalAlignment.RIGHT);
            subtotalNumStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            subtotalNumStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subtotalNumStyle.setBorderTop(BorderStyle.THIN);
            subtotalNumStyle.setBorderBottom(BorderStyle.DOUBLE);
            subtotalNumStyle.setBorderLeft(BorderStyle.THIN);
            subtotalNumStyle.setBorderRight(BorderStyle.THIN);

            // Percentage Style
            CellStyle subtotalPercentStyle = workbook.createCellStyle();
            subtotalPercentStyle.setFont(boldFont);
            subtotalPercentStyle.setDataFormat(percentFormat);
            subtotalPercentStyle.setAlignment(HorizontalAlignment.RIGHT);
            subtotalPercentStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            subtotalPercentStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorders(subtotalPercentStyle, BorderStyle.THIN);

            // --- BUILD ROWS ---
            int rowIdx = 0;

            // Row 0: Title
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(24);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("PROFIT & LOSS (INCOME STATEMENT) - " + matrix.getYear());
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Row 1: Subtitle metadata
            Row subTitleRow = sheet.createRow(rowIdx++);
            Cell subTitleCell = subTitleRow.createCell(0);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            subTitleCell.setCellValue("Currency: " + currencyCode + " (" + currSymbol + ") | Exported: " + timestamp + " | Status: Audited");
            CellStyle subStyle = workbook.createCellStyle();
            subStyle.setFont(subTitleFont);
            subTitleCell.setCellStyle(subStyle);

            rowIdx++; // Blank spacer row

            // Table Header Row
            List<String> periodLabels = matrix.getPeriodLabels();
            List<String> periods = matrix.getPeriods();
            int totalCols = 3 + periodLabels.size() + 1; // Item, Code, Subcategory, Periods..., Full Year

            Row tableHeaderRow = sheet.createRow(rowIdx++);
            tableHeaderRow.setHeightInPoints(22);

            String[] baseHeaders = {"Account / Line Item", "Code", "Subcategory"};
            for (int i = 0; i < baseHeaders.length; i++) {
                Cell c = tableHeaderRow.createCell(i);
                c.setCellValue(baseHeaders[i]);
                c.setCellStyle(headerStyle);
            }

            int colOffset = 3;
            for (int i = 0; i < periodLabels.size(); i++) {
                Cell c = tableHeaderRow.createCell(colOffset + i);
                c.setCellValue(periodLabels.get(i));
                c.setCellStyle(headerStyle);
            }

            Cell totalHeaderCell = tableHeaderRow.createCell(colOffset + periodLabels.size());
            totalHeaderCell.setCellValue("Full Year Total");
            totalHeaderCell.setCellStyle(headerStyle);

            // Sections
            if (matrix.getSections() != null) {
                for (PnlReportSectionDTO sec : matrix.getSections()) {
                    // Section title bar
                    Row secRow = sheet.createRow(rowIdx++);
                    secRow.setHeightInPoints(20);
                    Cell secCell = secRow.createCell(0);
                    secCell.setCellValue(sec.getTitle() != null ? sec.getTitle().toUpperCase() : sec.getSectionKey());
                    secCell.setCellStyle(sectionStyle);

                    for (int c = 1; c < totalCols; c++) {
                        Cell filler = secRow.createCell(c);
                        filler.setCellStyle(sectionStyle);
                    }
                    sheet.addMergedRegion(new CellRangeAddress(secRow.getRowNum(), secRow.getRowNum(), 0, totalCols - 1));

                    // Section account rows
                    if (sec.getRows() != null) {
                        for (PnlReportRowDTO r : sec.getRows()) {
                            writeReportRow(sheet.createRow(rowIdx++), r, periods, itemTextStyle, itemNumStyle, false);
                        }
                    }

                    // Section subtotal row
                    if (sec.getSubtotalRow() != null) {
                        writeReportRow(sheet.createRow(rowIdx++), sec.getSubtotalRow(), periods, subtotalTextStyle, subtotalNumStyle, false);
                    }
                    rowIdx++; // Blank separator
                }
            }

            // Calculated KPI Summary Rows
            if (matrix.getGrossProfitRow() != null) {
                writeReportRow(sheet.createRow(rowIdx++), matrix.getGrossProfitRow(), periods, subtotalTextStyle, subtotalNumStyle, false);
            }
            if (matrix.getGrossMarginPercentRow() != null) {
                writeReportRow(sheet.createRow(rowIdx++), matrix.getGrossMarginPercentRow(), periods, subtotalTextStyle, subtotalPercentStyle, true);
            }
            rowIdx++;

            if (matrix.getOperatingIncomeRow() != null) {
                writeReportRow(sheet.createRow(rowIdx++), matrix.getOperatingIncomeRow(), periods, subtotalTextStyle, subtotalNumStyle, false);
            }
            if (matrix.getOperatingMarginPercentRow() != null) {
                writeReportRow(sheet.createRow(rowIdx++), matrix.getOperatingMarginPercentRow(), periods, subtotalTextStyle, subtotalPercentStyle, true);
            }
            rowIdx++;

            if (matrix.getNetIncomeRow() != null) {
                writeReportRow(sheet.createRow(rowIdx++), matrix.getNetIncomeRow(), periods, subtotalTextStyle, subtotalNumStyle, false);
            }
            if (matrix.getNetMarginPercentRow() != null) {
                writeReportRow(sheet.createRow(rowIdx++), matrix.getNetMarginPercentRow(), periods, subtotalTextStyle, subtotalPercentStyle, true);
            }

            // Auto-size columns safely
            for (int col = 0; col < totalCols; col++) {
                try {
                    sheet.autoSizeColumn(col);
                    int currentWidth = sheet.getColumnWidth(col);
                    sheet.setColumnWidth(col, Math.max(currentWidth + 1024, 3800));
                } catch (Exception ignored) {
                    sheet.setColumnWidth(col, 4200);
                }
            }
            sheet.setColumnWidth(0, 9500); // Line item column wider

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void writeReportRow(Row row, PnlReportRowDTO r, List<String> periods, CellStyle textStyle, CellStyle numStyle, boolean isPercent) {
        row.setHeightInPoints(18);

        Cell c0 = row.createCell(0);
        c0.setCellValue(r.getName() != null ? r.getName() : "");
        c0.setCellStyle(textStyle);

        Cell c1 = row.createCell(1);
        c1.setCellValue(r.getCode() != null ? r.getCode() : "");
        c1.setCellStyle(textStyle);

        Cell c2 = row.createCell(2);
        c2.setCellValue(r.getSubcategory() != null ? r.getSubcategory() : "");
        c2.setCellStyle(textStyle);

        int colIdx = 3;
        for (String p : periods) {
            Cell c = row.createCell(colIdx++);
            BigDecimal val = r.getPeriodValues() != null ? r.getPeriodValues().get(p) : null;
            if (val != null) {
                if (isPercent) {
                    c.setCellValue(val.doubleValue() / 100.0);
                } else {
                    c.setCellValue(val.doubleValue());
                }
            } else {
                c.setCellValue(0.0);
            }
            c.setCellStyle(numStyle);
        }

        Cell cTotal = row.createCell(colIdx);
        if (r.getTotalValue() != null) {
            if (isPercent) {
                cTotal.setCellValue(r.getTotalValue().doubleValue() / 100.0);
            } else {
                cTotal.setCellValue(r.getTotalValue().doubleValue());
            }
        } else {
            cTotal.setCellValue(0.0);
        }
        cTotal.setCellStyle(numStyle);
    }

    private void setBorders(CellStyle style, BorderStyle border) {
        style.setBorderTop(border);
        style.setBorderBottom(border);
        style.setBorderLeft(border);
        style.setBorderRight(border);
    }
}
