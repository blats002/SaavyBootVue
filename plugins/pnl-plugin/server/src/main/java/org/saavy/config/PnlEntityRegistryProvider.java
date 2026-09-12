package org.saavy.config;

import org.saavy.component.EntityRegistryProvider;
import org.saavy.entity.PnlAccountDTO;
import org.saavy.entity.PnlLedgerEntryDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PnlEntityRegistryProvider implements EntityRegistryProvider {

    @Override
    public Map<String, Class<?>> getEntities() {
        return Map.of(
                "pnl-accounts", PnlAccountDTO.class,
                "pnl-ledger-entries", PnlLedgerEntryDTO.class
        );
    }
}
