package org.saavy.config;

import org.saavy.component.DashboardCardProvider;
import org.saavy.services.PnlDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PnlDashboardCardProvider implements DashboardCardProvider {

    @Autowired
    private PnlDashboardService pnlDashboardService;

    @Override
    public String getPluginName() {
        return "pnl-plugin";
    }

    @Override
    public List<Map<String, Object>> getCards() {
        return pnlDashboardService.buildDashboardCards();
    }
}
