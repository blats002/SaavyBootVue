package org.saavy.component;

import java.util.List;
import java.util.Map;

/**
 * Extension SPI for modules/plugins to contribute dashboard charts dynamically
 * to the central DashboardController.
 */
public interface DashboardChartProvider {
    List<Map<String, Object>> getCharts();

    default String getPluginName() {
        return null;
    }
}

