package org.saavy.config;

import org.saavy.component.DashboardChartProvider;
import org.saavy.services.PnlDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PnlDashboardChartProvider implements DashboardChartProvider {

    @Autowired
    private PnlDashboardService pnlDashboardService;

    @Override
    public String getPluginName() {
        return "pnl-plugin";
    }

    @Override
    public List<Map<String, Object>> getCharts() {
        return pnlDashboardService.buildDashboardCharts();
    }
}
