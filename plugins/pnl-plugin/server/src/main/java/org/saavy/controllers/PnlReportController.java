package org.saavy.controllers;

import org.saavy.dto.PnlIngestionBatchDTO;
import org.saavy.dto.PnlIngestionDTO;
import org.saavy.dto.PnlIngestionResultDTO;
import org.saavy.dto.PnlReportMatrixDTO;
import org.saavy.entity.PnlLedgerEntryDTO;
import org.saavy.services.PnlCsvIngestionService;
import org.saavy.services.PnlExcelExportService;
import org.saavy.services.PnlLedgerEntryService;
import org.saavy.services.PnlReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/pnl")
public class PnlReportController {

    @Autowired
    private PnlReportService pnlReportService;

    @Autowired
    private PnlExcelExportService pnlExcelExportService;

    @Autowired
    private PnlCsvIngestionService pnlCsvIngestionService;

    @Autowired
    private PnlLedgerEntryService pnlLedgerEntryService;

    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<PnlReportMatrixDTO> getReport(
            @RequestParam(name = "year", defaultValue = "2026") int year
    ) {
        PnlReportMatrixDTO matrix = pnlReportService.generateReportMatrix(year);
        return ResponseEntity.ok(matrix);
    }

    @GetMapping("/report/export/excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<?> exportExcel(
            @RequestParam(name = "year", defaultValue = "2026") int year
    ) {
        try {
            PnlReportMatrixDTO matrix = pnlReportService.generateReportMatrix(year);
            byte[] bytes = pnlExcelExportService.generateExcelReport(matrix);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"PnL_Statement_" + year + ".xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(java.util.Map.of("message", "Failed to generate Excel report: " + e.getMessage()));
        }
    }

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csv = pnlCsvIngestionService.generateSampleCsvTemplate();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pnl_ingestion_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @PostMapping("/ingest/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PnlIngestionResultDTO> ingestCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            PnlIngestionResultDTO err = PnlIngestionResultDTO.builder()
                    .success(false)
                    .errors(List.of("Uploaded file is empty."))
                    .build();
            return ResponseEntity.badRequest().body(err);
        }

        try (InputStream is = file.getInputStream()) {
            List<String> parseErrors = new ArrayList<>();
            List<PnlIngestionDTO> records = pnlCsvIngestionService.parseCsv(is, parseErrors);

            if (!parseErrors.isEmpty() && records.isEmpty()) {
                return ResponseEntity.badRequest().body(PnlIngestionResultDTO.builder()
                        .success(false)
                        .errors(parseErrors)
                        .build());
            }

            PnlIngestionBatchDTO batchDTO = new PnlIngestionBatchDTO(file.getOriginalFilename(), records);
            PnlIngestionResultDTO result = pnlCsvIngestionService.ingestBatch(batchDTO);
            result.getErrors().addAll(parseErrors);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(PnlIngestionResultDTO.builder()
                    .success(false)
                    .errors(List.of("CSV Processing Error: " + e.getMessage()))
                    .build());
        }
    }

    @PostMapping("/ingest/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<PnlIngestionResultDTO> ingestBatch(@RequestBody PnlIngestionBatchDTO batchDTO) {
        PnlIngestionResultDTO result = pnlCsvIngestionService.ingestBatch(batchDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/drilldown")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<PnlLedgerEntryDTO>> getDrilldown(
            @RequestParam(name = "accountId", required = false) Long accountId,
            @RequestParam(name = "period", required = false) String period,
            @RequestParam(name = "year", defaultValue = "2026") int year
    ) {
        LocalDate start;
        LocalDate end;

        if (period != null && period.matches("\\d{4}-\\d{2}")) {
            YearMonth ym = YearMonth.parse(period);
            start = ym.atDay(1);
            end = ym.atEndOfMonth();
        } else {
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        }

        List<PnlLedgerEntryDTO> list;
        if (accountId != null) {
            list = pnlLedgerEntryService.findByAccountIdAndDateRange(accountId, start, end);
        } else {
            list = pnlLedgerEntryService.findByDateRange(start, end);
        }
        return ResponseEntity.ok(list);
    }
}
