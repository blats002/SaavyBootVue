package org.saavy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.saavy.reference.PnlAccountCategory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PnlReportMatrixDTO implements Serializable {
    private int year;
    @Builder.Default
    private String currency = "PHP";
    @Builder.Default
    private List<String> periods = new ArrayList<>(); // e.g. ["2026-01", "2026-02", ... "2026-12"]
    @Builder.Default
    private List<String> periodLabels = new ArrayList<>(); // e.g. ["Jan 2026", "Feb 2026", ... "Dec 2026"]
    @Builder.Default
    private List<PnlReportSectionDTO> sections = new ArrayList<>();
    private PnlReportRowDTO grossProfitRow;
    private PnlReportRowDTO grossMarginPercentRow;
    private PnlReportRowDTO operatingIncomeRow;
    private PnlReportRowDTO operatingMarginPercentRow;
    private PnlReportRowDTO netIncomeRow;
    private PnlReportRowDTO netMarginPercentRow;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PnlReportSectionDTO implements Serializable {
        private String sectionKey; // REVENUE, COGS, OPEX, OTHER_INCOME, OTHER_EXPENSE, TAX
        private String title;
        private PnlAccountCategory category;
        @Builder.Default
        private List<PnlReportRowDTO> rows = new ArrayList<>();
        private PnlReportRowDTO subtotalRow;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PnlReportRowDTO implements Serializable {
        private Long accountId;
        private String code;
        private String name;
        private String subcategory;
        private boolean isCalculated;
        private boolean isHeader;
        private boolean isSubtotal;
        private boolean isPercentage;
        @Builder.Default
        private Map<String, BigDecimal> periodValues = new HashMap<>(); // periodKey -> amount
        private BigDecimal totalValue;
    }
}
