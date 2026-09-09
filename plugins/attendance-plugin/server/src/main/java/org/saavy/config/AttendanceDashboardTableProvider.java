package org.saavy.config;

import org.saavy.component.DashboardTableProvider;
import org.saavy.entity.AttendanceLog;
import org.saavy.services.AttendanceKioskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AttendanceDashboardTableProvider implements DashboardTableProvider {

    @Autowired
    private AttendanceKioskService attendanceKioskService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    @Override
    public String getPluginName() {
        return "attendance-plugin";
    }

    @Override
    public List<Map<String, Object>> getTables() {
        List<Map<String, Object>> tables = new ArrayList<>();

        try {
            List<AttendanceLog> logs = attendanceKioskService.getLiveFeed();

            List<Map<String, Object>> rows = logs.stream().map(log -> {
                Map<String, Object> row = new HashMap<>();
                row.put("id", log.getId());
                row.put("employeeName", log.getEmployeeName());
                row.put("department", log.getDepartment() != null ? log.getDepartment() : "General");
                row.put("time", log.getTimestamp() != null ? log.getTimestamp().format(TIME_FORMATTER) : "N/A");
                row.put("logType", log.getLogType() != null ? log.getLogType().name() : "N/A");
                row.put("status", log.getStatus() != null ? log.getStatus().name() : "ON_TIME");
                return row;
            }).collect(Collectors.toList());

            List<Map<String, Object>> columns = List.of(
                    Map.of("field", "employeeName", "header", "Employee", "sortable", true),
                    Map.of("field", "department", "header", "Department", "sortable", true),
                    Map.of("field", "time", "header", "Time", "sortable", true),
                    Map.of("field", "logType", "header", "Action", "type", "badge", "sortable", true,
                            "severityMap", Map.of("CLOCK_IN", "success", "CLOCK_OUT", "danger", "BREAK_OUT", "warning", "BREAK_IN", "info")),
                    Map.of("field", "status", "header", "Status", "type", "badge", "sortable", true,
                            "severityMap", Map.of("ON_TIME", "success", "LATE", "danger", "EARLY_DEPARTURE", "warning", "OVERTIME", "info"))
            );

            Map<String, Object> table = new HashMap<>();
            table.put("id", "live-attendance-feed-table");
            table.put("title", "Today's Live Attendance Feed");
            table.put("colSpan", "col-12 xl:col-6");
            table.put("paginator", true);
            table.put("rows", 5);
            table.put("columns", columns);
            table.put("data", rows);
            table.put("action", Map.of(
                    "label", "Open Bundy Clock",
                    "icon", "pi pi-qrcode",
                    "to", "/bundy-clock"
            ));

            tables.add(table);
        } catch (Exception e) {
            // Tables might not be populated yet
        }

        return tables;
    }
}
