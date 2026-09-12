package org.saavy.controllers;

import org.saavy.dto.PnlIngestionResultDTO;
import org.saavy.entity.PnlAccount;
import org.saavy.entity.PnlAccountDTO;
import org.saavy.services.JPAService;
import org.saavy.services.PnlAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/pnl-accounts")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class PnlAccountController extends BaseController<PnlAccount, PnlAccountDTO, Long> {

    @Autowired
    private PnlAccountService pnlAccountService;

    @Override
    protected JPAService<PnlAccount, PnlAccountDTO, Long> getService() {
        return pnlAccountService;
    }

    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        String csv = pnlAccountService.generateAccountCsvTemplate();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=pnl_accounts_template.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }

    @PostMapping("/ingest/csv")
    public ResponseEntity<PnlIngestionResultDTO> ingestCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(PnlIngestionResultDTO.builder()
                    .success(false)
                    .errors(List.of("Uploaded file is empty."))
                    .build());
        }

        try (InputStream is = file.getInputStream()) {
            PnlIngestionResultDTO result = pnlAccountService.importAccountsCsv(is);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(PnlIngestionResultDTO.builder()
                    .success(false)
                    .errors(List.of("Account CSV Error: " + e.getMessage()))
                    .build());
        }
    }
}
