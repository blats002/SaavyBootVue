package org.saavy.controllers;

import org.saavy.entity.PnlLedgerEntry;
import org.saavy.entity.PnlLedgerEntryDTO;
import org.saavy.services.JPAService;
import org.saavy.services.PnlLedgerEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pnl-ledger-entries")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
public class PnlLedgerEntryController extends BaseController<PnlLedgerEntry, PnlLedgerEntryDTO, Long> {

    @Autowired
    private PnlLedgerEntryService pnlLedgerEntryService;

    @Override
    protected JPAService<PnlLedgerEntry, PnlLedgerEntryDTO, Long> getService() {
        return pnlLedgerEntryService;
    }
}
