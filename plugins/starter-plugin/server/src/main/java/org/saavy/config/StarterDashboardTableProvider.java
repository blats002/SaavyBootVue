package org.saavy.config;

import org.saavy.component.DashboardTableProvider;
import org.saavy.entity.SampleItem;
import org.saavy.entity.SampleItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class StarterDashboardTableProvider implements DashboardTableProvider {

    @Autowired
    private SampleItemRepository sampleItemRepository;

    @Override
    public String getPluginName() {
        return "starter-plugin";
    }

    @Override
    public List<Map<String, Object>> getTables() {
        List<Map<String, Object>> tables = new ArrayList<>();

        Map<String, Object> table = new HashMap<>();
        table.put("id", "starter-sample-items-table");
        table.put("title", "Recent Sample Items");
        table.put("colSpan", "col-12 xl:col-6");
        table.put("paginator", true);
        table.put("rows", 5);

        List<Map<String, Object>> columns = new ArrayList<>();

        Map<String, Object> col1 = new HashMap<>();
        col1.put("field", "name");
        col1.put("header", "Name");
        col1.put("sortable", true);
        col1.put("width", "35%");

        Map<String, Object> col2 = new HashMap<>();
        col2.put("field", "description");
        col2.put("header", "Description");
        col2.put("sortable", true);
        col2.put("width", "35%");

        Map<String, Object> col3 = new HashMap<>();
        col3.put("field", "price");
        col3.put("header", "Price");
        col3.put("sortable", true);
        col3.put("type", "currency");
        col3.put("width", "15%");

        Map<String, Object> col4 = new HashMap<>();
        col4.put("field", "status");
        col4.put("header", "Status");
        col4.put("sortable", true);
        col4.put("type", "badge");
        col4.put("width", "15%");

        columns.add(col1);
        columns.add(col2);
        columns.add(col3);
        columns.add(col4);
        table.put("columns", columns);

        List<Map<String, Object>> data = new ArrayList<>();
        try {
            List<SampleItem> items = sampleItemRepository.findAll();
            if (items != null && !items.isEmpty()) {
                for (SampleItem item : items) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("name", item.getName());
                    row.put("description", item.getDescription());
                    row.put("price", item.getPrice());
                    row.put("status", Boolean.TRUE.equals(item.getActive()) ? "ACTIVE" : "INACTIVE");
                    data.add(row);
                }
            }
        } catch (Exception e) {
            // In case table is not populated yet
        }

        // Fallback sample rows if database table is empty
        if (data.isEmpty()) {
            data.add(Map.of("name", "Standard Widget", "description", "High quality industrial widget", "price", 49.99, "status", "ACTIVE"));
            data.add(Map.of("name", "Premium Gadget", "description", "Next-gen multi-tool", "price", 129.50, "status", "ACTIVE"));
            data.add(Map.of("name", "Eco Connector", "description", "Recycled material cable", "price", 19.00, "status", "PENDING"));
        }

        table.put("data", data);

        Map<String, Object> action = new HashMap<>();
        action.put("label", "Manage All");
        action.put("icon", "pi pi-external-link");
        action.put("to", "/pages/sample-items");
        table.put("action", action);

        tables.add(table);
        return tables;
    }
}
