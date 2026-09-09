package org.saavy.config;

import org.saavy.component.DashboardChartProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StarterDashboardChartProvider implements DashboardChartProvider {

    @Override
    public String getPluginName() {
        return "starter-plugin";
    }

    @Override
    public List<Map<String, Object>> getCharts() {
        List<Map<String, Object>> charts = new ArrayList<>();

        Map<String, Object> chart = new HashMap<>();
        chart.put("id", "starter-sales-trend");
        chart.put("title", "Monthly Sales Performance");
        chart.put("type", "line");
        chart.put("colSpan", "col-12 xl:col-6");

        Map<String, Object> data = new HashMap<>();
        data.put("labels", List.of("January", "February", "March", "April", "May", "June", "July"));

        List<Map<String, Object>> datasets = new ArrayList<>();

        Map<String, Object> dataset1 = new HashMap<>();
        dataset1.put("label", "Current Year");
        dataset1.put("data", List.of(65, 59, 80, 81, 56, 55, 72));
        dataset1.put("fill", false);
        dataset1.put("borderColor", "#00bb7e");
        dataset1.put("backgroundColor", "rgba(0, 187, 126, 0.2)");
        dataset1.put("tension", 0.4);

        Map<String, Object> dataset2 = new HashMap<>();
        dataset2.put("label", "Previous Year");
        dataset2.put("data", List.of(28, 48, 40, 19, 66, 27, 50));
        dataset2.put("fill", false);
        dataset2.put("borderColor", "#2f4860");
        dataset2.put("backgroundColor", "rgba(47, 72, 96, 0.2)");
        dataset2.put("tension", 0.4);

        datasets.add(dataset1);
        datasets.add(dataset2);
        data.put("datasets", datasets);

        chart.put("data", data);
        charts.add(chart);

        return charts;
    }
}
