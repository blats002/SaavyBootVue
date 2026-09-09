package org.saavy.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryDTO {
    private long totalActiveEmployees;
    private long presentToday;
    private long lateToday;
    private long onBreakToday;
    private long absentToday;
}
