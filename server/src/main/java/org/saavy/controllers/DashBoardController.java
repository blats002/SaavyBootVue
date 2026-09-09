package org.saavy.controllers;

import org.saavy.component.DashboardCardProvider;
import org.saavy.component.DashboardChartProvider;
import org.saavy.component.DashboardTableProvider;
import org.saavy.services.PluginConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class DashBoardController extends BaseDashBoardController {

    @Autowired(required = false)
    private List<DashboardCardProvider> cardProviders;

    @Autowired(required = false)
    private List<DashboardChartProvider> chartProviders;

    @Autowired(required = false)
    private List<DashboardTableProvider> tableProviders;

    @Autowired(required = false)
    private PluginConfigService pluginConfigService;

    private boolean isPluginActive(String pluginName) {
        if (pluginName == null || pluginName.isBlank() || pluginConfigService == null) {
            return true;
        }
        return pluginConfigService.isPluginEnabled(pluginName);
    }

    @Override
    public Map<String, String> getTabs() {
        if (pluginConfigService == null) {
            return Collections.emptyMap();
        }
        return pluginConfigService.getDashboardTitleMap();
    }

    @Override
    public List<Map<String, Object>> getCards() {
        List<Map<String, Object>> allCards = new ArrayList<>();
        if (cardProviders != null) {
            for (DashboardCardProvider provider : cardProviders) {
                try {
                    String pluginName = provider.getPluginName();
                    if (!isPluginActive(pluginName)) {
                        continue;
                    }
                    List<Map<String, Object>> cards = provider.getCards();
                    if (cards != null) {
                        for (Map<String, Object> card : cards) {
                            String itemPlugin = (String) card.get("plugin");
                            if (itemPlugin != null && !isPluginActive(itemPlugin)) {
                                continue;
                            }
                            if (pluginName != null && !card.containsKey("plugin")) {
                                card.put("plugin", pluginName);
                            }
                            allCards.add(card);
                        }
                    }
                } catch (Exception e) {
                    // Prevent single provider error from failing the whole dashboard endpoint
                }
            }
        }
        return allCards;
    }

    @Override
    public List<Map<String, Object>> getCharts() {
        List<Map<String, Object>> allCharts = new ArrayList<>();
        if (chartProviders != null) {
            for (DashboardChartProvider provider : chartProviders) {
                try {
                    String pluginName = provider.getPluginName();
                    if (!isPluginActive(pluginName)) {
                        continue;
                    }
                    List<Map<String, Object>> charts = provider.getCharts();
                    if (charts != null) {
                        for (Map<String, Object> chart : charts) {
                            String itemPlugin = (String) chart.get("plugin");
                            if (itemPlugin != null && !isPluginActive(itemPlugin)) {
                                continue;
                            }
                            if (pluginName != null && !chart.containsKey("plugin")) {
                                chart.put("plugin", pluginName);
                            }
                            allCharts.add(chart);
                        }
                    }
                } catch (Exception e) {
                    // Prevent single provider error from failing the whole dashboard endpoint
                }
            }
        }
        return allCharts;
    }

    @Override
    public List<Map<String, Object>> getTables() {
        List<Map<String, Object>> allTables = new ArrayList<>();
        if (tableProviders != null) {
            for (DashboardTableProvider provider : tableProviders) {
                try {
                    String pluginName = provider.getPluginName();
                    if (!isPluginActive(pluginName)) {
                        continue;
                    }
                    List<Map<String, Object>> tables = provider.getTables();
                    if (tables != null) {
                        for (Map<String, Object> table : tables) {
                            String itemPlugin = (String) table.get("plugin");
                            if (itemPlugin != null && !isPluginActive(itemPlugin)) {
                                continue;
                            }
                            if (pluginName != null && !table.containsKey("plugin")) {
                                table.put("plugin", pluginName);
                            }
                            allTables.add(table);
                        }
                    }
                } catch (Exception e) {
                    // Prevent single provider error from failing the whole dashboard endpoint
                }
            }
        }
        return allTables;
    }
}
