package org.saavy.component;

import java.util.List;
import java.util.Map;

/**
 * Extension SPI for modules/plugins to contribute dashboard summary tables dynamically
 * to the central DashboardController.
 */
public interface DashboardTableProvider {
    List<Map<String, Object>> getTables();

    default String getPluginName() {
        return null;
    }
}

