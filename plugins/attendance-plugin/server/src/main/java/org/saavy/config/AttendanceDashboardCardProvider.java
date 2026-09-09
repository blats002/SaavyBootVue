package org.saavy.config;

import org.saavy.component.DashboardCardProvider;
import org.saavy.entity.AttendanceSummaryDTO;
import org.saavy.services.AttendanceKioskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AttendanceDashboardCardProvider implements DashboardCardProvider {

    @Autowired
    private AttendanceKioskService attendanceKioskService;

    @Override
    public String getPluginName() {
        return "attendance-plugin";
    }

    @Override
    public List<Map<String, Object>> getCards() {
        List<Map<String, Object>> cards = new ArrayList<>();

        try {
            AttendanceSummaryDTO summary = attendanceKioskService.getTodaySummary();

            // 1. Present Today Card
            Map<String, Object> presentCard = new HashMap<>();
            presentCard.put("title", "Present Today");
            presentCard.put("content", summary.getPresentToday() + " / " + summary.getTotalActiveEmployees() + " Checked In");
            presentCard.put("icon", "pi pi-users text-green-500 text-xl");

            double presentPct = summary.getTotalActiveEmployees() > 0
                    ? ((double) summary.getPresentToday() / summary.getTotalActiveEmployees()) * 100.0
                    : 0.0;

            Map<String, Object> presentFooter = new HashMap<>();
            presentFooter.put("value", String.format("%.0f%%", presentPct));
            presentFooter.put("content", "attendance turnout today");
            presentFooter.put("severity", presentPct >= 80 ? "success" : presentPct >= 50 ? "warning" : "danger");
            presentCard.put("footer", presentFooter);
            cards.add(presentCard);

            // 2. Late Arrivals Card
            Map<String, Object> lateCard = new HashMap<>();
            lateCard.put("title", "Late Arrivals");
            lateCard.put("content", summary.getLateToday() == 0 ? "All On-Time Today" : summary.getLateToday() + " Employee(s) Late");
            lateCard.put("icon", "pi pi-clock text-orange-500 text-xl");

            Map<String, Object> lateFooter = new HashMap<>();
            lateFooter.put("value", summary.getLateToday() > 0 ? String.valueOf(summary.getLateToday()) : "");
            lateFooter.put("content", summary.getLateToday() == 0 ? "Zero shift tardiness" : "clocked in after 9:15 AM");
            lateFooter.put("severity", summary.getLateToday() == 0 ? "success" : "danger");
            lateCard.put("footer", lateFooter);
            cards.add(lateCard);

        } catch (Exception e) {
            // Tables might not be populated yet
        }

        return cards;
    }
}
