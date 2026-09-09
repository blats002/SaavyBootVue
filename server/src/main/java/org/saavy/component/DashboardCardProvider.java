package org.saavy.component;

import java.util.List;
import java.util.Map;

/**
 * Extension SPI for modules/plugins to contribute dashboard cards dynamically
 * to the central DashboardController.
 */
public interface DashboardCardProvider {
    List<Map<String, Object>> getCards();

    default String getPluginName() {
        return null;
    }
}

