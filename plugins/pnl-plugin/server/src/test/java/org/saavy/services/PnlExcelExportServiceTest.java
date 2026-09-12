package org.saavy.services;

import org.junit.jupiter.api.Test;
import org.saavy.dto.PnlReportMatrixDTO;
import org.saavy.dto.PnlReportMatrixDTO.PnlReportRowDTO;
import org.saavy.dto.PnlReportMatrixDTO.PnlReportSectionDTO;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PnlExcelExportServiceTest {

    @Test
    public void testGenerateExcelReport() throws Exception {
        PnlExcelExportService service = new PnlExcelExportService();

        PnlReportMatrixDTO matrix = PnlReportMatrixDTO.builder()
                .year(2026)
                .currency("PHP")
                .periods(List.of("2026-01", "2026-02", "2026-03", "2026-04", "2026-05", "2026-06", "2026-07", "2026-08", "2026-09", "2026-10", "2026-11", "2026-12"))
                .periodLabels(List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))
                .sections(List.of(
                        PnlReportSectionDTO.builder()
                                .sectionKey("REVENUE")
                                .title("Revenue / Inflows")
                                .rows(List.of(
                                        PnlReportRowDTO.builder()
                                                .accountId(1L)
                                                .code("REV-01")
                                                .name("Sales")
                                                .subcategory("Core")
                                                .periodValues(Map.of("2026-01", new BigDecimal("10000.50")))
                                                .totalValue(new BigDecimal("10000.50"))
                                                .build()
                                ))
                                .subtotalRow(PnlReportRowDTO.builder()
                                        .name("Total Revenue")
                                        .periodValues(Map.of("2026-01", new BigDecimal("10000.50")))
                                        .totalValue(new BigDecimal("10000.50"))
                                        .build())
                                .build()
                ))
                .grossProfitRow(PnlReportRowDTO.builder()
                        .name("Gross Profit")
                        .periodValues(Map.of("2026-01", new BigDecimal("10000.50")))
                        .totalValue(new BigDecimal("10000.50"))
                        .build())
                .grossMarginPercentRow(PnlReportRowDTO.builder()
                        .name("Gross Margin %")
                        .periodValues(Map.of("2026-01", new BigDecimal("50.0")))
                        .totalValue(new BigDecimal("50.0"))
                        .build())
                .netIncomeRow(PnlReportRowDTO.builder()
                        .name("Net Income")
                        .periodValues(Map.of("2026-01", new BigDecimal("5000.00")))
                        .totalValue(new BigDecimal("5000.00"))
                        .build())
                .netMarginPercentRow(PnlReportRowDTO.builder()
                        .name("Net Margin %")
                        .periodValues(Map.of("2026-01", new BigDecimal("25.0")))
                        .totalValue(new BigDecimal("25.0"))
                        .build())
                .build();

        byte[] bytes = service.generateExcelReport(matrix);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (FileOutputStream fos = new FileOutputStream("test_pnl.xlsx")) {
            fos.write(bytes);
        }
    }
}
